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

// Elementos de validación del registro
const registerEmail           = document.getElementById("registerEmail");
const registerPassword        = document.getElementById("registerPassword");
const confirmRegisterPassword = document.getElementById("confirmRegisterPassword");
const emailError              = document.getElementById("emailError");
const confirmError            = document.getElementById("confirmError");

let currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");
let shopProducts = [];

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
    products.innerHTML = `<p class="muted">${escapeHtml(text)}</p>`;
    renderInventory();
}

// ─── Validaciones ─────────────────────────────────────────────────────────────

function isValidEmail(v) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
}

function pwMissing(password) {
    const missing = [];
    if (password.length < 8)                                             missing.push("mínimo 8 caracteres");
    if (!/[A-Z]/.test(password))                                         missing.push("una mayúscula");
    if (!/[0-9]/.test(password))                                         missing.push("un número");
    if (!/[!@#$%^&*()\-_=+\[\]{};:'"\\|,.<>/?]/.test(password))        missing.push("un carácter especial");
    return missing;
}

// Email: error en tiempo real
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

// ─── API ──────────────────────────────────────────────────────────────────────

async function authRequest(path, body) {
    const response = await fetch(`${AUTH_API}${path}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    if (!response.ok) {
        throw new Error(String(response.status));
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
        products.innerHTML = '<p class="muted">Los productos apareceran al iniciar sesion.</p>';
        renderInventory();
        return;
    }

    try {
        await refreshCurrentUser();
        userInfo.textContent = `${currentUser.nombre} - ${currentUser.ects} ECTS`;
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

    try {
        currentUser = await authRequest("/login", {
            id:       document.getElementById("loginId").value,
            password: document.getElementById("loginPassword").value
        });
        localStorage.setItem("currentUser", JSON.stringify(currentUser));
        loginForm.reset();
        setMessage("Sesion iniciada correctamente.", "ok");
        await renderSession();
    } catch (error) {
        setMessage("Usuario o password incorrectos.", "error");
    }
});

registerForm.addEventListener("submit", async event => {
    event.preventDefault();

    const email    = registerEmail.value.trim();
    const password = registerPassword.value;
    const confirm  = confirmRegisterPassword.value;

    // 1. Validar email
    if (!isValidEmail(email)) {
        emailError.classList.remove("hidden");
        setMessage("Introduce un correo electronico valido.", "error");
        return;
    }

    // 2. Validar robustez de contraseña — mostrar en el cuadro inferior qué falta
    const missing = pwMissing(password);
    if (missing.length > 0) {
        setMessage("Contraseña insegura. Falta: " + missing.join(", ") + ".", "error");
        return;
    }

    // 3. Validar que las contraseñas coincidan
    if (password !== confirm) {
        confirmError.classList.remove("hidden");
        setMessage("Las contraseñas no coinciden.", "error");
        return;
    }

    setMessage("Creando cuenta...", "");

    try {
        currentUser = await authRequest("/register", {
            id:       document.getElementById("registerId").value,
            nombre:   document.getElementById("registerName").value,
            email:    email,
            password: password
        });
        localStorage.setItem("currentUser", JSON.stringify(currentUser));
        registerForm.reset();
        setMode("login");
        setMessage("Cuenta creada. Ya estas dentro.", "ok");
        await renderSession();
    } catch (error) {
        if (error.message === "409") {
            setMessage("Ese usuario o correo electronico ya esta registrado.", "error");
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

showLogin.addEventListener("click", () => setMode("login"));
showRegister.addEventListener("click", () => setMode("register"));
renderSession();