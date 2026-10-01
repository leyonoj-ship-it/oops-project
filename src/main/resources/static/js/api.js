// =========================================================
// SMARTDINE — Central API Client
// Handles authentication tokens, error trapping, and endpoints
// =========================================================

const API_BASE = '/api';

const api = {
    getToken() {
        return localStorage.getItem('smartdine_token');
    },

    getUser() {
        const userJson = localStorage.getItem('smartdine_user');
        return userJson ? JSON.parse(userJson) : null;
    },

    async request(endpoint, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const res = await fetch(`${API_BASE}${endpoint}`, config);
            if (!res.ok) {
                const errData = await res.json().catch(() => ({ message: res.statusText }));
                throw new Error(errData.message || 'Request failed');
            }
            if (res.status === 204) return null;
            return await res.json();
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
    },

    // Auth
    login(payload) {
        return this.request('/auth/login', { method: 'POST', body: JSON.stringify(payload) });
    },
    googleVerify(payload) {
        return this.request('/auth/google-verify', { method: 'POST', body: JSON.stringify(payload) });
    },
    quickDemo(role, establishmentId = null) {
        return this.request('/auth/quick-demo', { method: 'POST', body: JSON.stringify({ role, establishmentId }) });
    },
    getCurrentUser() {
        return this.request('/auth/me');
    },

    // Establishments (Hotels & Canteens)
    getEstablishments() {
        return this.request('/establishments');
    },
    getEstablishmentById(id) {
        return this.request(`/establishments/${id}`);
    },
    getNearbyEstablishments(lat, lon, radius = 25, type = null) {
        let url = `/establishments/nearby?lat=${lat}&lon=${lon}&radius=${radius}`;
        if (type) url += `&type=${type}`;
        return this.request(url);
    },
    getEstablishmentsByType(type) {
        return this.request(`/establishments/types/${type}`);
    },
    searchEstablishments(query) {
        return this.request(`/establishments/search?query=${encodeURIComponent(query)}`);
    },
    getEstablishmentsByAmbiance(ambiance) {
        return this.request(`/establishments/ambiance/${ambiance}`);
    },
    getEstablishmentDashboard(id) {
        return this.request(`/establishments/${id}/dashboard`);
    },

    // Food & Menu
    getMenu(establishmentId) {
        return this.request(`/foods/establishment/${establishmentId}`);
    },
    getFoodItem(id) {
        return this.request(`/foods/${id}`);
    },
    addFoodItem(data) {
        return this.request('/foods', { method: 'POST', body: JSON.stringify(data) });
    },
    updateFoodItem(id, data) {
        return this.request(`/foods/${id}`, { method: 'PUT', body: JSON.stringify(data) });
    },
    deleteFoodItem(id) {
        return this.request(`/foods/${id}`, { method: 'DELETE' });
    },
    toggleFoodAvailability(id) {
        return this.request(`/foods/${id}/toggle-availability`, { method: 'PATCH' });
    },

    // Smart Food Clearance Offers
    createClearanceOffer(data) {
        return this.request('/foods/clearance-offers', { method: 'POST', body: JSON.stringify(data) });
    },
    getActiveClearances() {
        return this.request('/foods/clearance-offers/active');
    },
    getEstablishmentClearances(establishmentId) {
        return this.request(`/foods/clearance-offers/establishment/${establishmentId}`);
    },

    // Recommendations
    getFoodRecommendations(ambiance, establishmentId = null) {
        let url = `/foods/recommendations?ambiance=${ambiance}`;
        if (establishmentId) url += `&establishmentId=${establishmentId}`;
        return this.request(url);
    },
    getCategories() {
        return this.request('/foods/categories');
    },

    // Orders & Fast Serve
    placeOrder(data) {
        return this.request('/orders', { method: 'POST', body: JSON.stringify(data) });
    },
    getOrder(id) {
        return this.request(`/orders/${id}`);
    },
    getOrdersByCustomer(customerId) {
        return this.request(`/orders/customer/${customerId}`);
    },
    getOrdersByEstablishment(establishmentId) {
        return this.request(`/orders/establishment/${establishmentId}`);
    },
    getFastServeOrders(establishmentId) {
        return this.request(`/orders/establishment/${establishmentId}/fast-serve`);
    },
    updateOrderStatus(orderId, status) {
        return this.request(`/orders/${orderId}/status?status=${status}`, { method: 'PATCH' });
    },
    cancelOrder(orderId, reason = '') {
        return this.request(`/orders/${orderId}/cancel?reason=${encodeURIComponent(reason)}`, { method: 'POST' });
    },

    // Reservations
    createReservation(data) {
        return this.request('/reservations', { method: 'POST', body: JSON.stringify(data) });
    },
    getReservationsByCustomer(customerId) {
        return this.request(`/reservations/customer/${customerId}`);
    },
    getReservationsByEstablishment(establishmentId) {
        return this.request(`/reservations/establishment/${establishmentId}`);
    },
    updateReservationStatus(id, status) {
        return this.request(`/reservations/${id}/status?status=${status}`, { method: 'PATCH' });
    },

    // Reviews
    submitReview(data) {
        return this.request('/reviews', { method: 'POST', body: JSON.stringify(data) });
    },
    getReviews(establishmentId) {
        return this.request(`/reviews/establishment/${establishmentId}`);
    },
    getFlaggedReviews() {
        return this.request('/reviews/flagged');
    },
    flagReview(id) {
        return this.request(`/reviews/${id}/flag`, { method: 'POST' });
    },
    deleteReview(id) {
        return this.request(`/reviews/${id}`, { method: 'DELETE' });
    },

    // Admin
    getAdminDashboard(timeRange = 'all') {
        return this.request(`/admin/dashboard?timeRange=${timeRange}`);
    },
    getAllUsers(search = '') {
        return this.request(`/admin/users?search=${encodeURIComponent(search)}`);
    },
    updateUserStatus(userId, status) {
        return this.request(`/admin/users/${userId}/status?status=${status}`, { method: 'PATCH' });
    },
    updateEstablishmentStatus(establishmentId, status) {
        return this.request(`/admin/establishments/${establishmentId}/status?status=${status}`, { method: 'PATCH' });
    }
};

// UI Toast Helper
function showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';
    if (type === 'fast-serve') icon = '⚡';

    toast.innerHTML = `<span>${icon}</span> <div>${message}</div>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease reverse forwards';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}
