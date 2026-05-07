async function handleLogin() {
    const user = document.getElementById("Usuario").value;
    const password = document.getElementById("password").value;
    const messageDiv = document.getElementById("message");

    const loginData = {
        id: user,
        password: password
    };

    try {
        const response = await fetch("http://localhost:8080/dsaApp/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(loginData)
        });

        if (response.ok) {
            messageDiv.className = "message ok";
            messageDiv.innerText = "Login exitoso. Redirigiendo...";
            window.location.href = "http://localhost:8080/swagger/index.html";
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
