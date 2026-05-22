const AUTH_API = "/dsaApp/auth";
const SHOP_API = "/dsaApp/tienda";

const loginForm    = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");
const showLogin    = document.getElementById("showLogin");
const showRegister = document.getElementById("showRegister");
const message      = document.getElementById("message");
const userInfo     = document.getElementById("userInfo");
const gameStateInfo = document.getElementById("gameStateInfo");
const products     = document.getElementById("products");
const inventory    = document.getElementById("inventory");
const ranking      = document.getElementById("ranking");
const missions     = document.getElementById("missions");
const logoutButton = document.getElementById("logoutButton");
const registerAvatar = document.getElementById("registerAvatar");
const registerAvatarPicker = document.getElementById("registerAvatarPicker");
const sessionAvatarPanel = document.getElementById("sessionAvatarPanel");
const sessionAvatarPicker = document.getElementById("sessionAvatarPicker");
const currentAvatarImage = document.getElementById("currentAvatarImage");
const changeAvatarButton = document.getElementById("changeAvatarButton");

// Pestañas
const mainTabs     = document.getElementById("mainTabs");
const tabShop      = document.getElementById("tabShop");
const tabRanking   = document.getElementById("tabRanking");
const tabMissions  = document.getElementById("tabMissions");
const viewShop     = document.getElementById("viewShop");
const viewRanking  = document.getElementById("viewRanking");
const viewMissions = document.getElementById("viewMissions");

// Elementos de validación del registro
const registerEmail           = document.getElementById("registerEmail");
const registerPassword        = document.getElementById("registerPassword");
const confirmRegisterPassword = document.getElementById("confirmRegisterPassword");
const emailError              = document.getElementById("emailError");
const confirmError            = document.getElementById("confirmError");
const passwordHelp            = document.getElementById("passwordHelp");

let currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");
let shopProducts = [];
let rankingPlayers = [];
let missionList = [];
const AVATARS = [
    "avatar_1","avatar_2","avatar_3","avatar_4","avatar_5","avatar_6",
    "avatar_7","avatar_8","avatar_9","avatar_10","avatar_11","avatar_12"
];

// ─── Pestañas ─────────────────────────────────────────────────────────────────

function setTab(tab) {
    const views = { shop: viewShop, ranking: viewRanking, missions: viewMissions };
    const btns  = { shop: tabShop,  ranking: tabRanking,  missions: tabMissions  };

    Object.entries(views).forEach(([key, el]) => {
        el.classList.toggle("hidden", key !== tab);
    });
    Object.entries(btns).forEach(([key, btn]) => {
        btn.classList.toggle("secondary", key !== tab);
        btn.classList.toggle("active", key === tab);
    });
}

tabShop?.addEventListener("click",     () => setTab("shop"));
tabRanking?.addEventListener("click",  async () => {
    setTab("ranking");
    if (!rankingPlayers.length) await loadRanking();
});
tabMissions?.addEventListener("click", async () => {
    setTab("missions");
    if (!missionList.length) await loadMissions();
});

// ─── Utilidades ───────────────────────────────────────────────────────────────

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function setMessage(text, type) {
    message.textContent = text;
    message.className = type ? `message ${type}` : "message";
}

function setMode(mode) {
    const loginMode = mode === "login";
    loginForm.classList.toggle("hidden", !loginMode);
    registerForm.classList.toggle("hidden", loginMode);
    showLogin.classList.toggle("secondary", !loginMode);
    showRegister.classList.toggle("secondary", loginMode);
    setMessage("", "");
}

function renderShellState() {
    document.body.classList.toggle("session-active", Boolean(currentUser));
    mainTabs?.classList.toggle("hidden", !currentUser);
}

function clearSession(text) {
    currentUser = null;
    localStorage.clear();
    renderShellState();
    userInfo.textContent = "Inicia sesion para ver tu saldo.";
    logoutButton.classList.add("hidden");
    renderAvatarState();
    renderGameState();
    products.innerHTML = `<p class="muted">${escapeHtml(text)}</p>`;
    ranking.innerHTML = '<p class="muted">Inicia sesion para ver el ranking.</p>';
    missions.innerHTML = '<p class="muted">Las misiones apareceran al iniciar sesion.</p>';
    renderInventory();
    setTab("shop");
}

function cleanAvatar(avatar) {
    return AVATARS.includes(avatar) ? avatar : "avatar_1";
}

function avatarPath(avatar) {
    return `img/avatars/${cleanAvatar(avatar)}.png`;
}

function markSelectedAvatar(container, avatar) {
    if (!container) return;
    const selectedAvatar = cleanAvatar(avatar);
    container.querySelectorAll(".avatar-option").forEach(button => {
        button.classList.toggle("selected", button.dataset.avatar === selectedAvatar);
    });
}

function renderAvatarState() {
    const avatar = cleanAvatar(currentUser?.avatar);
    markSelectedAvatar(registerAvatarPicker, registerAvatar?.value || "avatar_1");
    markSelectedAvatar(sessionAvatarPicker, avatar);

    if (!currentUser) {
        sessionAvatarPanel?.classList.add("hidden");
        sessionAvatarPicker?.classList.add("hidden");
        if (changeAvatarButton) changeAvatarButton.textContent = "Cambiar avatar";
        currentAvatarImage?.classList.add("hidden");
        return;
    }

    sessionAvatarPanel?.classList.remove("hidden");
    currentAvatarImage.src = avatarPath(avatar);
    currentAvatarImage.classList.remove("hidden");
}

function renderGameState() {
    if (!gameStateInfo) return;

    const state = currentUser?.gameState;
    if (!currentUser || !state) {
        gameStateInfo.classList.add("hidden");
        gameStateInfo.innerHTML = "";
        return;
    }

    const health = state.health ?? 100;
    const maxHealth = state.maxHealth ?? 100;
    const mission = state.currentMissionTitle || `Mision ${state.currentMissionId || 1}`;
    const objective = state.currentObjectiveTitle || `Objetivo ${state.currentObjectiveId || 1}`;
    const healthPercent = Math.max(0, Math.min(100, (health / Math.max(maxHealth, 1)) * 100));

    gameStateInfo.classList.remove("hidden");
    gameStateInfo.innerHTML = `
        <div class="health-row">
            <span>Vida</span>
            <strong>${escapeHtml(health)} / ${escapeHtml(maxHealth)}</strong>
        </div>
        <div class="health-track" aria-hidden="true">
            <span style="width: ${healthPercent}%"></span>
        </div>
        <div class="mission-row">
            <span>${escapeHtml(mission)}</span>
            <small>${escapeHtml(objective)}</small>
        </div>
    `;
}

async function updateAvatar(avatar) {
    if (!currentUser) return;

    const selectedAvatar = cleanAvatar(avatar);
    const response = await fetch(`${AUTH_API}/usuarios/${encodeURIComponent(currentUser.id)}/avatar/${encodeURIComponent(selectedAvatar)}`, {
        method: "PUT"
    });

    if (!response.ok) throw new Error(String(response.status));

    currentUser = await response.json();
    localStorage.setItem("currentUser", JSON.stringify(currentUser));
    renderAvatarState();
    sessionAvatarPicker?.classList.add("hidden");
    if (changeAvatarButton) changeAvatarButton.textContent = "Cambiar avatar";
}

// ─── Validaciones ─────────────────────────────────────────────────────────────

function isValidEmail(v) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
}

function pwMissing(password) {
    const missing = [];
    if (password.length < 8)                                             missing.push("minimo 8 caracteres");
    if (!/[A-Z]/.test(password))                                         missing.push("una mayuscula");
    if (!/[0-9]/.test(password))                                         missing.push("un numero");
    if (!/[!@#$%^&*()\-_=+\[\]{};:'"\\|,.<>/?]/.test(password))        missing.push("un caracter especial");
    return missing;
}

function backendPasswordMissing(detail) {
    const labels = {
        MIN_LENGTH: "minimo 8 caracteres",
        UPPERCASE:  "una mayuscula",
        NUMBER:     "un numero",
        SPECIAL:    "un caracter especial"
    };
    return String(detail || "")
        .replace("WEAK_PASSWORD:", "")
        .split(",")
        .map(rule => labels[rule.trim()])
        .filter(Boolean);
}

function updatePasswordHelp() {
    const missing = pwMissing(registerPassword.value);
    passwordHelp.classList.toggle("hidden", !registerPassword.value || missing.length === 0);
    passwordHelp.textContent = missing.length === 0 ? "" : "Falta: " + missing.join(", ") + ".";
}

registerEmail.addEventListener("input", () => {
    const v = registerEmail.value;
    emailError.classList.toggle("hidden", !v || isValidEmail(v));
});
registerEmail.addEventListener("blur", () => {
    emailError.classList.toggle("hidden", isValidEmail(registerEmail.value));
});

confirmRegisterPassword.addEventListener("input", () => {
    const match = registerPassword.value === confirmRegisterPassword.value;
    confirmError.classList.toggle("hidden", match || !confirmRegisterPassword.value);
});

registerPassword.addEventListener("input", () => {
    updatePasswordHelp();
    const match = registerPassword.value === confirmRegisterPassword.value;
    confirmError.classList.toggle("hidden", match || !confirmRegisterPassword.value);
});

// ─── API ──────────────────────────────────────────────────────────────────────

async function authRequest(path, body) {
    const response = await fetch(`${AUTH_API}${path}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    if (!response.ok) {
        const detail = await readErrorDetail(response);
        const error = new Error(String(response.status));
        error.detail = detail.code || detail.raw || "";
        error.messageText = detail.message || "";
        throw error;
    }

    return response.json();
}

async function readErrorDetail(response) {
    const raw = await response.text();
    if (!raw) return { code: "", message: "", raw: "" };
    try {
        const parsed = JSON.parse(raw);
        return { code: parsed.code || "", message: parsed.message || "", raw };
    } catch {
        return { code: raw, message: raw, raw };
    }
}

async function refreshCurrentUser() {
    const response = await fetch(`${AUTH_API}/usuarios/${encodeURIComponent(currentUser.id)}`);
    if (!response.ok) throw new Error(String(response.status));
    currentUser = await response.json();
    localStorage.setItem("currentUser", JSON.stringify(currentUser));
}

async function loadRanking() {
    if (!ranking) return;
    try {
        const response = await fetch(`${AUTH_API}/ranking`);
        if (!response.ok) {
            ranking.innerHTML = '<p class="muted">No se ha podido cargar el ranking.</p>';
            return;
        }
        rankingPlayers = await response.json();
        renderRanking();
    } catch {
        ranking.innerHTML = '<p class="muted">No se ha podido cargar el ranking.</p>';
    }
}

async function loadMissions() {
    if (!missions) return;
    try {
        const response = await fetch(`${AUTH_API}/misiones`);
        if (!response.ok) {
            missions.innerHTML = '<p class="muted">No se han podido cargar las misiones.</p>';
            return;
        }
        missionList = await response.json();
        renderMissions();
    } catch {
        missions.innerHTML = '<p class="muted">No se han podido cargar las misiones.</p>';
    }
}

// ─── Tienda ───────────────────────────────────────────────────────────────────

async function loadProducts() {
    const response = await fetch(`${SHOP_API}/productos`);
    if (!response.ok) {
        products.innerHTML = '<p class="muted">No se han podido cargar los productos.</p>';
        return;
    }
    shopProducts = await response.json();
    renderProducts();
}

function renderProducts() {
    if (!shopProducts.length) {
        products.innerHTML = '<p class="muted">No hay objetos disponibles en la tienda.</p>';
        return;
    }

    products.innerHTML = shopProducts.map(product => {
        const canBuy = currentUser && currentUser.ects >= product.precio;
        const buttonText = canBuy ? "Comprar" : "ECTS insuficientes";

        return `
            <article class="product">
                <div class="product-code">OBJ-${escapeHtml(product.id)}</div>
                <h3>${escapeHtml(product.nombre)}</h3>
                <p>${escapeHtml(product.descripcion)}</p>
                <div class="product-actions">
                    <strong>${escapeHtml(product.precio)} ECTS</strong>
                    <button type="button" data-product-id="${escapeHtml(product.id)}" ${canBuy ? "" : "disabled"}>
                        ${buttonText}
                    </button>
                </div>
            </article> 
        `;
    }).join("");
}


function renderRanking() {
    if (!ranking) return;
    if (!currentUser) {
        ranking.innerHTML = '<p class="muted">Inicia sesion para ver el ranking.</p>';
        return;
    }
    if (!rankingPlayers.length) {
        ranking.innerHTML = '<p class="muted">Todavia no hay jugadores en el ranking.</p>';
        return;
    }

    ranking.innerHTML = rankingPlayers.map((player, index) => {
        const state = player.gameState || {};
        const missionId = state.currentMissionId ?? 1;
        const objectiveId = state.currentObjectiveId ?? 1;
        const missionTitle = state.currentMissionTitle || `Mision ${missionId}`;
        const isCurrent = player.id === currentUser.id;

        return `
            <article class="ranking-row ${isCurrent ? "current-player" : ""}">
                <span class="rank-position">#${index + 1}</span>
                <img src="${avatarPath(player.avatar)}" alt="Avatar de ${escapeHtml(player.nombre || player.id)}">
                <div>
                    <strong>${escapeHtml(player.nombre || player.id)}</strong>
                    <small>${escapeHtml(missionTitle)} - Objetivo ${escapeHtml(objectiveId)}</small>
                </div>
                <span class="rank-level">Nivel ${escapeHtml(missionId)}</span>
            </article>
        `;
    }).join("");
}

function renderMissions() {
    if (!missions) return;
    if (!currentUser) {
        missions.innerHTML = '<p class="muted">Las misiones apareceran al iniciar sesion.</p>';
        return;
    }
    if (!missionList.length) {
        missions.innerHTML = '<p class="muted">No hay misiones configuradas todavia.</p>';
        return;
    }

    const currentMission = currentUser.gameState?.currentMissionId ?? 1;
    const currentObjective = currentUser.gameState?.currentObjectiveId ?? 1;

    missions.innerHTML = missionList.map(mission => {
        const missionId = mission.id ?? mission.missionOrder ?? 1;
        const isCurrent = missionId === currentMission;
        const isCompleted = missionId < currentMission;
        const isLocked = missionId > currentMission;
        const status = isCompleted ? "Completada" : (isCurrent ? "Activa" : "Bloqueada");
        const objectives = mission.objectives || [];

        return `
            <article class="mission-card ${isCurrent ? "active" : ""} ${isLocked ? "locked" : ""}">
                <div class="mission-card-header">
                    <div>
                        <span class="mission-index">Mision ${escapeHtml(missionId)}</span>
                        <h4>${escapeHtml(mission.title)}</h4>
                    </div>
                    <span class="mission-status">${status}</span>
                </div>
                <p>${escapeHtml(mission.description)}</p>
                <div class="objective-list">
                    ${objectives.length ? objectives.map(objective => `
                        <span class="${objective.id === currentObjective && isCurrent ? "current-objective" : ""}">
                            ${escapeHtml(objective.title)}
                            ${objective.reward ? `<small>+${escapeHtml(objective.reward)} ECTS</small>` : ""}
                        </span>
                    `).join("") : '<span class="muted">Sin objetivos definidos.</span>'}
                </div>
            </article>
        `;
    }).join("");
}

function renderInventory() {
    if (!inventory) return;

    const items = currentUser?.inventario || [];
    if (!items.length) {
        inventory.innerHTML = '<p class="muted">Compra objetos para desbloquear nuevas pistas.</p>';
        return;
    }

    const grouped = {};
    items.forEach(item => {
        const key = item.id ?? item.nombre;
        if (grouped[key]) {
            grouped[key].cantidad++;
        } else {
            grouped[key] = { ...item, cantidad: 1 };
        }
    });

    inventory.innerHTML = Object.values(grouped).map(item => `
        <span class="inventory-chip">
            ${escapeHtml(item.nombre)}${item.cantidad > 1 ? ` <span class="inventory-qty">x${item.cantidad}</span>` : ""}
            <button class="inventory-delete" type="button" data-inventory-product-id="${escapeHtml(item.id)}" aria-label="Eliminar ${escapeHtml(item.nombre)}">
                Eliminar
            </button>
        </span>
    `).join("");
}

async function deleteInventoryProduct(productId) {
    if (!currentUser) {
        setMessage("Inicia sesion para eliminar objetos.", "error");
        return;
    }
    setMessage("Eliminando objeto del inventario...", "");
    try {
        const response = await fetch(`${SHOP_API}/inventario/${encodeURIComponent(productId)}/${encodeURIComponent(currentUser.id)}`, {
            method: "DELETE"
        });
        if (!response.ok) throw new Error(String(response.status));
        await refreshCurrentUser();
        userInfo.textContent = `${currentUser.nombre} - ${currentUser.ects} ECTS`;
        renderProducts();
        renderInventory();
        setMessage("Objeto eliminado del inventario.", "ok");
    } catch (error) {
        if (error.message === "404") {
            await renderSession();
            setMessage("Ese objeto ya no esta en tu inventario.", "error");
            return;
        }
        setMessage("No se ha podido eliminar el objeto.", "error");
    }
}

async function buyProduct(productId) {
    if (!currentUser) {
        setMessage("Inicia sesion para comprar objetos.", "error");
        return;
    }
    setMessage("Procesando compra en la tienda SIGMA...", "");
    try {
        await refreshCurrentUser();
        const response = await fetch(`${SHOP_API}/comprar/${encodeURIComponent(productId)}/${encodeURIComponent(currentUser.id)}`, {
            method: "POST"
        });
        if (!response.ok) throw new Error(String(response.status));
        await refreshCurrentUser();
        userInfo.textContent = `${currentUser.nombre} - ${currentUser.ects} ECTS`;
        renderAvatarState();
        renderGameState();
        renderProducts();
        rankingPlayers = [];
        missionList = [];
        renderInventory();
        setMessage("Objeto adquirido. Inventario sincronizado.", "ok");
    } catch (error) {
        if (error.message === "404") {
            clearSession("La sesion guardada ya no existe en el servidor.");
            setMessage("Sesion caducada. Inicia sesion o registrate de nuevo.", "error");
            return;
        }
        setMessage(error.message === "402" ? "No tienes suficientes ECTS para este objeto." : "No se ha podido completar la compra.", "error");
    }
}

async function renderSession() {
    renderShellState();

    if (!currentUser) {
        userInfo.textContent = "Inicia sesion para ver tu saldo.";
        logoutButton.classList.add("hidden");
        renderAvatarState();
        renderGameState();
        products.innerHTML = '<p class="muted">Los productos apareceran al iniciar sesion.</p>';
        ranking.innerHTML = '<p class="muted">Inicia sesion para ver el ranking.</p>';
        missions.innerHTML = '<p class="muted">Las misiones apareceran al iniciar sesion.</p>';
        renderInventory();
        setTab("shop");
        return;
    }

    // Refrescar usuario — si falla, limpiar sesión
    try {
        await refreshCurrentUser();
    } catch {
        clearSession("Los productos apareceran al iniciar sesion.");
        return;
    }

    renderShellState();
    userInfo.textContent = `${currentUser.nombre} - ${currentUser.ects} ECTS`;
    renderAvatarState();
    renderGameState();
    logoutButton.classList.remove("hidden");
    setTab("shop");

    // Cargar tienda, ranking y misiones de forma independiente
    // para que un fallo en uno no rompa los demás
    await loadProducts();
    renderInventory();
    await loadRanking();
    await loadMissions();
}

// ─── Formularios ──────────────────────────────────────────────────────────────

loginForm.addEventListener("submit", async event => {
    event.preventDefault();
    setMessage("Entrando...", "");

    const loginId  = document.getElementById("loginId").value.trim();
    const password = document.getElementById("loginPassword").value;

    try {
        currentUser = await authRequest("/login", isValidEmail(loginId)
            ? { email: loginId.toLowerCase(), password }
            : { id: loginId, password });

        localStorage.setItem("currentUser", JSON.stringify(currentUser));
        loginForm.reset();
        setMessage("Sesion iniciada correctamente.", "ok");
        await renderSession();
    } catch {
        setMessage("Usuario, correo o contrasena incorrectos.", "error");
    }
});

registerForm.addEventListener("submit", async event => {
    event.preventDefault();

    const email    = registerEmail.value.trim().toLowerCase();
    const password = registerPassword.value;
    const confirm  = confirmRegisterPassword.value;

    if (!isValidEmail(email)) {
        emailError.classList.remove("hidden");
        setMessage("Introduce un correo electronico valido.", "error");
        return;
    }

    const missing = pwMissing(password);
    if (missing.length > 0) {
        updatePasswordHelp();
        setMessage("Contrasena insegura. Falta: " + missing.join(", ") + ".", "error");
        return;
    }

    if (password !== confirm) {
        confirmError.classList.remove("hidden");
        setMessage("Las contrasenas no coinciden.", "error");
        return;
    }

    setMessage("Creando cuenta...", "");

    try {
        currentUser = await authRequest("/register", {
            id:       document.getElementById("registerId").value.trim(),
            nombre:   document.getElementById("registerName").value.trim(),
            email,
            password,
            avatar:   registerAvatar.value
        });
        localStorage.setItem("currentUser", JSON.stringify(currentUser));
        registerForm.reset();
        registerAvatar.value = "avatar_1";
        renderAvatarState();
        updatePasswordHelp();
        setMode("login");
        setMessage("Cuenta creada. Ya estas dentro.", "ok");
        await renderSession();
    } catch (error) {
        if (error.message === "409") {
            setMessage(error.detail?.includes("EMAIL_EXISTS")
                ? "Ese correo electronico ya esta registrado."
                : "Ese nombre de usuario ya existe.", "error");
        } else if (error.message === "400" && error.detail?.includes("WEAK_PASSWORD")) {
            setMessage("Contrasena insegura. Falta: " + backendPasswordMissing(error.detail).join(", ") + ".", "error");
        } else if (error.message === "400" && error.detail?.includes("INVALID_EMAIL")) {
            setMessage("Introduce un correo electronico valido.", "error");
        } else if (error.message === "400") {
            setMessage("Rellena usuario, correo y contrasena.", "error");
        } else if (error.message === "500") {
            setMessage("No se ha podido crear la cuenta por un error del servidor.", "error");
        } else {
            setMessage("Revisa los datos del registro.", "error");
        }
    }
});

logoutButton.addEventListener("click", () => {
    currentUser = null;
    localStorage.clear();
    setMessage("Sesion cerrada.", "ok");
    renderSession();
});

products.addEventListener("click", event => {
    const button = event.target.closest("button[data-product-id]");
    if (button) buyProduct(button.dataset.productId);
});

inventory?.addEventListener("click", event => {
    const button = event.target.closest("button[data-inventory-product-id]");
    if (button) deleteInventoryProduct(button.dataset.inventoryProductId);
});

registerAvatarPicker?.addEventListener("click", event => {
    const button = event.target.closest(".avatar-option");
    if (!button) return;
    registerAvatar.value = cleanAvatar(button.dataset.avatar);
    markSelectedAvatar(registerAvatarPicker, registerAvatar.value);
});

sessionAvatarPicker?.addEventListener("click", async event => {
    const button = event.target.closest(".avatar-option");
    if (!button) return;
    try {
        await updateAvatar(button.dataset.avatar);
        setMessage("Avatar actualizado.", "ok");
    } catch {
        setMessage("No se ha podido actualizar el avatar.", "error");
    }
});

changeAvatarButton?.addEventListener("click", () => {
    const isHidden = sessionAvatarPicker.classList.toggle("hidden");
    changeAvatarButton.textContent = isHidden ? "Cambiar avatar" : "Cerrar avatares";
});

showLogin.addEventListener("click", () => setMode("login"));
showRegister.addEventListener("click", () => setMode("register"));
renderSession();
