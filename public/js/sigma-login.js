const AUTH_API = "http://localhost:8080/dsaApp/auth";

async function handleLogin() {
    const user = document.getElementById("usuario").value;   // minúscula
    const password = document.getElementById("password").value;
    const messageDiv = document.getElementById("message");

    const loginData = {
        id: user,
        password: password
    };

    try {
        const response = await fetch(`${AUTH_API}/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(loginData)
        });

        if (response.ok) {
            const currentUser = await response.json();
            localStorage.setItem("currentUser", JSON.stringify(currentUser));
            messageDiv.className = "message ok";
            messageDiv.innerText = "Login exitoso. Redirigiendo...";
            window.location.href = "index.html";
        } else {
            messageDiv.className = "message error";
            messageDiv.innerText = "Error: usuario o contrasena incorrectos.";
        }
    } catch (error) {
        messageDiv.className = "message error";
        messageDiv.innerText = "Error de conexion con el servidor.";
        console.error(error);
    }
}

document.getElementById("loginFormDedicado").addEventListener("submit", event => {
    event.preventDefault();
    handleLogin();
});