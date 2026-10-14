document.addEventListener('DOMContentLoaded', () => {
    const formLogin = document.getElementById('form-login');
    const formRegistro = document.getElementById('form-registro');
    const errorEl = document.getElementById('mensaje-error');
    const errorRegistro = document.getElementById('mensaje-registro');
    const btn = document.getElementById('btn-login');
    const btnRegistro = document.getElementById('btn-registro');
    const btnTextoInicial = '<span>Entrar</span>';
    const btnRegistroTextoInicial = '<span>Crear cuenta</span>';
    const subtitulo = document.getElementById('login-subtitulo');

    const setError = (el, msg, exito = false) => {
        el.className = 'alerta' + (exito ? ' alerta-exito' : '');
        if (!msg) {
            el.innerHTML = '';
            return;
        }
        el.innerHTML = icono(exito ? 'check' : 'alerta') + '<span></span>';
        el.querySelector('span').textContent = msg;
    };

    const alternar = (mostrarRegistro) => {
        formLogin.classList.toggle('oculto', mostrarRegistro);
        formRegistro.classList.toggle('oculto', !mostrarRegistro);
        setError(errorEl, '');
        setError(errorRegistro, '');
        subtitulo.textContent = mostrarRegistro
            ? 'Crea tu cuenta y empieza a consultar la biblioteca'
            : 'Inicia sesión para administrar la biblioteca';
        (mostrarRegistro ? document.getElementById('reg-nombre') : document.getElementById('username')).focus();
    };

    document.getElementById('btn-mostrar-registro').addEventListener('click', () => alternar(true));
    document.getElementById('btn-mostrar-login').addEventListener('click', () => alternar(false));

    formLogin.addEventListener('submit', async (e) => {
        e.preventDefault();
        setError(errorEl, '');

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        btn.disabled = true;
        btn.setAttribute('aria-busy', 'true');
        btn.innerHTML = spinnerBoton() + '<span>Ingresando...</span>';

        try {
            const resp = await apiRequest('/auth/login', 'POST', { username, password });
            localStorage.setItem('token', resp.data.token);
            localStorage.setItem('usuario', JSON.stringify(resp.data));
            window.location.href = 'panel.html';
        } catch (err) {
            setError(errorEl, err.message);
        } finally {
            btn.disabled = false;
            btn.removeAttribute('aria-busy');
            btn.innerHTML = btnTextoInicial;
        }
    });

    formRegistro.addEventListener('submit', async (e) => {
        e.preventDefault();
        setError(errorRegistro, '');

        const nombre = document.getElementById('reg-nombre').value.trim();
        const apellido = document.getElementById('reg-apellido').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const username = document.getElementById('reg-username').value.trim();
        const password = document.getElementById('reg-password').value;
        const password2 = document.getElementById('reg-password2').value;

        if (username.length < 3) {
            setError(errorRegistro, 'El nombre de usuario debe tener al menos 3 caracteres');
            return;
        }
        if (password.length < 6) {
            setError(errorRegistro, 'La contraseña debe tener al menos 6 caracteres');
            return;
        }
        if (password !== password2) {
            setError(errorRegistro, 'Las contraseñas no coinciden');
            return;
        }

        btnRegistro.disabled = true;
        btnRegistro.setAttribute('aria-busy', 'true');
        btnRegistro.innerHTML = spinnerBoton() + '<span>Creando...</span>';

        try {
            const resp = await apiRequest('/auth/registro', 'POST', {
                username, password, nombre, apellido, email
            });
            document.getElementById('reg-username').value = username;
            document.getElementById('reg-password').value = '';
            document.getElementById('reg-password2').value = '';
            document.getElementById('username').value = username;
            document.getElementById('password').value = '';
            alternar(false);
            setError(errorEl, resp.message, true);
        } catch (err) {
            setError(errorRegistro, err.message);
        } finally {
            btnRegistro.disabled = false;
            btnRegistro.removeAttribute('aria-busy');
            btnRegistro.innerHTML = btnRegistroTextoInicial;
        }
    });
});