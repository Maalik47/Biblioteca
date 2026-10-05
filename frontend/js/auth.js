document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-login');
    const errorEl = document.getElementById('mensaje-error');
    const btn = document.getElementById('btn-login');
    const btnTextoInicial = '<span>Entrar</span>';

    const setError = (msg) => {
        if (!msg) {
            errorEl.innerHTML = '';
            return;
        }
        errorEl.innerHTML = icono('alerta') + '<span></span>';
        errorEl.querySelector('span').textContent = msg;
    };

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        setError('');

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
            setError(err.message);
        } finally {
            btn.disabled = false;
            btn.removeAttribute('aria-busy');
            btn.innerHTML = btnTextoInicial;
        }
    });
});