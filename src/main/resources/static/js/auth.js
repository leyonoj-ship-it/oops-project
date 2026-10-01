// =========================================================
// SMARTDINE — Authentication & Role Guard System
// =========================================================

const auth = {
    selectedRole: 'CUSTOMER',

    initRoleSelector() {
        const roleCards = document.querySelectorAll('.role-select-card');
        roleCards.forEach(card => {
            card.addEventListener('click', () => {
                roleCards.forEach(c => c.classList.remove('selected'));
                card.classList.add('selected');
                this.selectedRole = card.dataset.role;
                this.updateRoleUI();
            });
        });
    },

    updateRoleUI() {
        const roleLabel = document.getElementById('selectedRoleLabel');
        if (roleLabel) {
            if (this.selectedRole === 'CUSTOMER') roleLabel.textContent = 'User / Customer Login';
            else if (this.selectedRole === 'ESTABLISHMENT_OWNER') roleLabel.textContent = 'Hotel & Canteen Manager Login';
            else if (this.selectedRole === 'ADMIN') roleLabel.textContent = 'Platform Administrator Login';
        }

        const estSelectorGroup = document.getElementById('establishmentSelectGroup');
        if (estSelectorGroup) {
            estSelectorGroup.style.display = (this.selectedRole === 'ESTABLISHMENT_OWNER') ? 'block' : 'none';
        }
    },

    async handleLogin(email, establishmentId = null) {
        try {
            const role = this.selectedRole;
            const res = await api.login({ email, role, establishmentId });
            this.saveSession(res);
            this.redirectByRole(res.role);
        } catch (error) {
            showToast(error.message, 'error');
        }
    },

    async handleQuickDemo(role, establishmentId = null) {
        try {
            const res = await api.quickDemo(role, establishmentId);
            this.saveSession(res);
            this.redirectByRole(res.role);
        } catch (error) {
            showToast('Demo login error: ' + error.message, 'error');
        }
    },

    async handleGoogleAuth(googleUser) {
        try {
            const email = googleUser?.email || document.getElementById('loginEmail')?.value;
            const name = googleUser?.name || 'Google User';
            const googleToken = googleUser?.token || 'mock-google-id-token-' + Date.now();

            const res = await api.googleVerify({
                email,
                name,
                role: this.selectedRole,
                googleToken
            });
            this.saveSession(res);
            this.redirectByRole(res.role);
        } catch (error) {
            showToast('Google Sign-In failed: ' + error.message, 'error');
        }
    },

    saveSession(authResponse) {
        localStorage.setItem('smartdine_token', authResponse.token);
        localStorage.setItem('smartdine_user', JSON.stringify({
            userId: authResponse.userId,
            name: authResponse.name,
            email: authResponse.email,
            role: authResponse.role,
            status: authResponse.status,
            establishmentId: authResponse.establishmentId,
            establishmentName: authResponse.establishmentName,
            establishmentType: authResponse.establishmentType
        }));
    },

    redirectByRole(role) {
        if (role === 'CUSTOMER') {
            window.location.href = 'customer.html';
        } else if (role === 'ESTABLISHMENT_OWNER') {
            window.location.href = 'establishment.html';
        } else if (role === 'ADMIN') {
            window.location.href = 'admin.html';
        } else {
            window.location.href = 'index.html';
        }
    },

    logout() {
        localStorage.removeItem('smartdine_token');
        localStorage.removeItem('smartdine_user');
        window.location.href = 'index.html';
    },

    requireRole(expectedRole) {
        const user = api.getUser();
        const token = api.getToken();
        if (!token || !user) {
            window.location.href = 'index.html';
            return null;
        }
        if (user.role !== expectedRole) {
            showToast('Access denied: Unauthorized role.', 'error');
            setTimeout(() => {
                this.redirectByRole(user.role);
            }, 1000);
            return null;
        }
        return user;
    }
};
