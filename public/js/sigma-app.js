const AUTH_API = "/dsaApp/auth";
const SHOP_API = "/dsaApp/tienda";

const loginForm    = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");
const showLogin    = document.getElementById("showLogin");
const showRegister = document.getElementById("showRegister");
const message      = document.getElementById("message");
const userInfo     = document.getElementById("userInfo");
const products     = document.getElementById("products");
const inventory    = document.getElementById("inventory");
const logoutButton = document.getElementById("logoutButton");
const registerAvatar = document.getElementById("registerAvatar");
const registerAvatarPicker = document.getElementById("registerAvatarPicker");
const sessionAvatarPanel = document.getElementById("sessionAvatarPanel");
const sessionAvatarPicker = document.getElementById("sessionAvatarPicker");
const currentAvatarImage = document.getElementById("currentAvatarImage");

// Elementos de validación del registro
const registerEmail           = document.getElementById("registerEmail");
const registerPassword        = document.getElementById("registerPassword");
const confirmRegisterPassword = document.getElementById("confirmRegisterPassword");
const emailError              = document.getElementById("emailError");
const confirmError            = document.getElementById("confirmError");
const passwordHelp            = document.getElementById("passwordHelp");

let currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");
let shopProducts = [];
const AVATARS = ["avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5", "avatar_6"];

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

function clearSession(text) {
    currentUser = null;
    localStorage.clear();
    userInfo.textContent = "Inicia sesion para ver tu saldo.";
    logoutButton.classList.add("hidden");
    renderAvatarState();
    products.innerHTML = `<p class="muted">${escapeHtml(text)}</p>`;
    renderInventory();
}

function cleanAvatar(avatar) {
    return AVATARS.includes(avatar) ? avatar : "avatar_1";
}

function avatarPath(avatar) {
    return `img/avatars/${cleanAvatar(avatar)}.png`;
}

function markSelectedAvatar(container, avatar) {
    if (!container) {
        return;
    }

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
        currentAvatarImage?.classList.add("hidden");
        return;
    }

    sessionAvatarPanel?.classList.remove("hidden");
    currentAvatarImage.src = avatarPath(avatar);
    currentAvatarImage.classList.remove("hidden");
}

async function updateAvatar(avatar) {
    if (!currentUser) {
        return;
    }

    const selectedAvatar = cleanAvatar(avatar);
    const response = await fetch(`${AUTH_API}/usuarios/${encodeURIComponent(currentUser.id)}/avatar/${encodeURIComponent(selectedAvatar)}`, {
        method: "PUT"
    });

    if (!response.ok) {
        throw new Error(String(response.status));
    }

    currentUser = await response.json();
    localStorage.setItem("currentUser", JSON.stringify(currentUser));
    renderAvatarState();
}

// ─── Validaciones ─────────────────────────────────────────────────────────────

function isValidEmail(v) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
}

function pwMissing(password) {
    const missing = [];
    if (password.length < 8)                                              missing.push("mínimo 8 caracteres");
    if (!/[A-Z]/.test(password))                                          missing.push("una mayúscula");
    if (!/[0-9]/.test(password))                                          missing.push("un número");
    if (!/[!@#$%^&*()\-_=+\[\]{};:'"\\|,.<>/?]/.test(password))         missing.push("un carácter especial");
    return missing;
}

function pwMissing(password) {
    const missing = [];
    if (password.length < 8)                                      missing.push("minimo 8 caracteres");
    if (!/[A-Z]/.test(password))                                  missing.push("una mayuscula");
    if (!/[0-9]/.test(password))                                  missing.push("un numero");
    if (!/[!@#$%^&*()\-_=+\[\]{};:'"\\|,.<>/?]/.test(password)) missing.push("un caracter especial");
    return missing;
}

function backendPasswordMissing(detail) {
    const labels = {
        MIN_LENGTH: "minimo 8 caracteres",
        UPPERCASE: "una mayuscula",
        NUMBER: "un numero",
        SPECIAL: "un caracter especial"
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

// Email registro: error en tiempo real
registerEmail.addEventListener("input", () => {
    const v = registerEmail.value;
    emailError.classList.toggle("hidden", !v || isValidEmail(v));
});
registerEmail.addEventListener("blur", () => {
    emailError.classList.toggle("hidden", isValidEmail(registerEmail.value));
});

// Confirmar contraseña: error en tiempo real
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
        const detail = await response.text();
        const error = new Error(String(response.status));
        error.detail = detail;
        throw error;
    }

    return response.json();
}

async function refreshCurrentUser() {
    const response = await fetch(`${AUTH_API}/usuarios/${encodeURIComponent(currentUser.id)}`);
    if (!response.ok) {
        throw new Error(String(response.status));
    }

    currentUser = await response.json();
    localStorage.setItem("currentUser", JSON.stringify(currentUser));
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

function renderInventory() {
    if (!inventory) {
        return;
    }

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
        </span>
    `).join("");
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

        if (!response.ok) {
            throw new Error(String(response.status));
        }

        await refreshCurrentUser();
        userInfo.textContent = `${currentUser.nombre} - ${currentUser.ects} ECTS`;
        renderAvatarState();
        renderProducts();
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
    if (!currentUser) {
        userInfo.textContent = "Inicia sesion para ver tu saldo.";
        logoutButton.classList.add("hidden");
        renderAvatarState();
        products.innerHTML = '<p class="muted">Los productos apareceran al iniciar sesion.</p>';
        renderInventory();
        return;
    }

    try {
        await refreshCurrentUser();
        userInfo.textContent = `${currentUser.nombre} - ${currentUser.ects} ECTS`;
        renderAvatarState();
        logoutButton.classList.remove("hidden");
        await loadProducts();
        renderInventory();
    } catch (error) {
        clearSession("Los productos apareceran al iniciar sesion.");
        setMessage("", "");
    }
}

// ─── Formularios ──────────────────────────────────────────────────────────────

loginForm.addEventListener("submit", async event => {
    event.preventDefault();
    setMessage("Entrando...", "");

    const loginId  = document.getElementById("loginId").value.trim();
    const password = document.getElementById("loginPassword").value;

    try {
        // Si lo que escribió tiene formato email, usar el endpoint login-by-email
        currentUser = await authRequest("/login", isValidEmail(loginId)
            ? { email: loginId.toLowerCase(), password: password }
            : { id: loginId, password: password });

        localStorage.setItem("currentUser", JSON.stringify(currentUser));
        loginForm.reset();
        setMessage("Sesion iniciada correctamente.", "ok");
        await renderSession();
    } catch (error) {
        setMessage("Usuario, correo o contrasena incorrectos.", "error");
    }
});

registerForm.addEventListener("submit", async event => {
    event.preventDefault();

    const email    = registerEmail.value.trim().toLowerCase();
    const password = registerPassword.value;
    const confirm  = confirmRegisterPassword.value;

    // 1. Validar email
    if (!isValidEmail(email)) {
        emailError.classList.remove("hidden");
        setMessage("Introduce un correo electronico valido.", "error");
        return;
    }

    // 2. Validar robustez de contraseña
    const missing = pwMissing(password);
    if (missing.length > 0) {
        updatePasswordHelp();
        setMessage("Contrasena insegura. Falta: " + missing.join(", ") + ".", "error");
        return;
    }

    // 3. Validar que las contraseñas coincidan
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
            email:    email,
            password: password,
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
            if (error.detail.includes("EMAIL_EXISTS")) {
                setMessage("Ese correo electronico ya esta registrado.", "error");
            } else {
                setMessage("Ese nombre de usuario ya existe.", "error");
            }
        } else if (error.message === "400" && error.detail.includes("WEAK_PASSWORD")) {
            const missingFromServer = backendPasswordMissing(error.detail);
            setMessage("Contrasena insegura. Falta: " + missingFromServer.join(", ") + ".", "error");
        } else if (error.message === "400" && error.detail.includes("INVALID_EMAIL")) {
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
    if (!button) {
        return;
    }

    buyProduct(button.dataset.productId);
});

registerAvatarPicker?.addEventListener("click", event => {
    const button = event.target.closest(".avatar-option");
    if (!button) {
        return;
    }

    registerAvatar.value = cleanAvatar(button.dataset.avatar);
    markSelectedAvatar(registerAvatarPicker, registerAvatar.value);
});

sessionAvatarPicker?.addEventListener("click", async event => {
    const button = event.target.closest(".avatar-option");
    if (!button) {
        return;
    }

    try {
        await updateAvatar(button.dataset.avatar);
        setMessage("Avatar actualizado.", "ok");
    } catch (error) {
        setMessage("No se ha podido actualizar el avatar.", "error");
    }
});

showLogin.addEventListener("click", () => setMode("login"));
showRegister.addEventListener("click", () => setMode("register"));
renderSession();
