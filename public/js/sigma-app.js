const AUTH_API = "/dsaApp/auth";
const SHOP_API = "/dsaApp/tienda";

const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");
const showLogin = document.getElementById("showLogin");
const showRegister = document.getElementById("showRegister");
const message = document.getElementById("message");
const userInfo = document.getElementById("userInfo");
const products = document.getElementById("products");
const inventory = document.getElementById("inventory");
const logoutButton = document.getElementById("logoutButton");

let currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");
let shopProducts = [];

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

    // Agrupar productos repetidos y contar cuántas veces aparecen
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
        // Servidor reiniciado o sesión inválida: limpiar sin mostrar error
        clearSession("Los productos apareceran al iniciar sesion.");
        setMessage("", "");
    }
}

loginForm.addEventListener("submit", async event => {
    event.preventDefault();
    setMessage("Entrando...", "");

    try {
        currentUser = await authRequest("/login", {
            id: document.getElementById("loginId").value,
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
    setMessage("Creando cuenta...", "");

    try {
        currentUser = await authRequest("/register", {
            id: document.getElementById("registerId").value,
            nombre: document.getElementById("registerName").value,
            password: document.getElementById("registerPassword").value
        });
        localStorage.setItem("currentUser", JSON.stringify(currentUser));
        registerForm.reset();
        setMode("login");
        setMessage("Cuenta creada. Ya estas dentro.", "ok");
        await renderSession();
    } catch (error) {
        setMessage(error.message === "409" ? "Ese usuario ya existe." : "Revisa los datos del registro.", "error");
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