/**
 * CHINNAMMAL — SMART MANAGEMENT PLATFORM
 * Authentication & Login Flow Controller
 */

document.addEventListener('DOMContentLoaded', async () => {
  const loginForm = document.getElementById('loginForm');
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');
  const togglePasswordBtn = document.getElementById('togglePassword');
  const loginError = document.getElementById('loginError');
  const loginBtn = document.getElementById('loginBtn');

  // 1. Check if Initial Administrator Setup is required
  try {
    const setupCheck = await fetch('/api/auth/setup-required').then(r => r.json());
    if (setupCheck && setupCheck.success && setupCheck.data && setupCheck.data.setupRequired) {
      window.location.replace('/setup.html');
      return;
    }
  } catch (err) {
    console.error('Setup check error', err);
  }

  // 2. Check if already authenticated
  try {
    const authCheck = await fetch('/api/auth/me').then(r => r.json());
    if (authCheck && authCheck.success && authCheck.data) {
      if (authCheck.data.forcePasswordChange) {
        window.location.replace('/change-password.html');
        return;
      }
      redirectUserByRole(authCheck.data.role);
      return;
    }
  } catch (ignored) {}

  // Toggle password visibility
  if (togglePasswordBtn) {
    togglePasswordBtn.addEventListener('click', () => {
      const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
      passwordInput.setAttribute('type', type);
      togglePasswordBtn.textContent = type === 'password' ? '👁️' : '🔒';
    });
  }

  // Handle Login Submit
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      loginError.style.display = 'none';

      const username = usernameInput.value.trim();
      const password = passwordInput.value;

      if (!username || !password) {
        showError('Please enter both username and password.');
        return;
      }

      setLoading(true);

      try {
        const res = await App.API.post('/api/auth/login', { username, password });
        if (res && res.success && res.data) {
          const user = res.data;

          // If temporary password, redirect to mandatory Change Password page
          if (user.forcePasswordChange) {
            setLoading(false);
            App.Toast.info('Temporary password detected. Redirecting to set permanent password...');
            setTimeout(() => {
              window.location.replace('/change-password.html');
            }, 600);
            return;
          }

          App.Toast.success(`Welcome back, ${user.displayName || user.username}!`);
          setTimeout(() => {
            redirectUserByRole(user.role);
          }, 400);
        }
      } catch (err) {
        setLoading(false);
        showError(err.message || 'Invalid username or password.');
      }
    });
  }

  function showError(msg) {
    if (loginError) {
      loginError.textContent = msg;
      loginError.style.display = 'block';
    }
  }

  function setLoading(isLoading) {
    if (loginBtn) {
      if (isLoading) {
        loginBtn.disabled = true;
        loginBtn.innerHTML = '<span class="spinner"></span> Signing in...';
      } else {
        loginBtn.disabled = false;
        loginBtn.innerHTML = 'Sign In';
      }
    }
  }

  function redirectUserByRole(role) {
    if (role === 'ADMIN') {
      window.location.replace('/admin.html');
    } else if (role === 'WARDEN') {
      window.location.replace('/warden.html');
    } else if (role === 'STUDENT') {
      window.location.replace('/student.html');
    } else {
      window.location.replace('/admin.html');
    }
  }
});
