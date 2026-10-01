// =========================================================
// SMARTDINE — Platform Administrator Controller
// =========================================================

const adminApp = {
    user: null,
    stats: null,
    users: [],
    establishments: [],
    flaggedReviews: [],
    currentRange: 'all',

    async init() {
        this.user = auth.requireRole('ADMIN');
        if (!this.user) return;

        this.initNavTabs();
        this.initTurnoverFilter();
        await this.loadDashboardData();
        await this.loadUsers();
        await this.loadEstablishments();
        await this.loadFlaggedReviews();
    },

    initNavTabs() {
        const navLinks = document.querySelectorAll('.sidebar-nav-item');
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                navLinks.forEach(l => l.classList.remove('active'));
                link.classList.add('active');
                const tab = link.dataset.tab;
                document.querySelectorAll('.tab-section').forEach(sec => sec.style.display = 'none');
                const target = document.getElementById(`tab-${tab}`);
                if (target) target.style.display = 'block';
            });
        });
    },

    initTurnoverFilter() {
        const filterSelect = document.getElementById('turnoverRangeFilter');
        if (filterSelect) {
            filterSelect.addEventListener('change', (e) => {
                this.currentRange = e.target.value;
                this.updateTurnoverDisplay();
            });
        }
    },

    async loadDashboardData() {
        try {
            this.stats = await api.getAdminDashboard(this.currentRange);
            this.renderKPIs();
            this.renderTopEstablishments();
        } catch (e) {
            console.error('Failed to load admin stats:', e);
        }
    },

    renderKPIs() {
        if (!this.stats) return;
        const setVal = (id, val) => {
            const el = document.getElementById(id);
            if (el) el.textContent = val;
        };

        setVal('adminTotalUsers', this.stats.totalUsers);
        setVal('adminNewUsers', `+${this.stats.newUsers} this week`);
        setVal('adminTotalEst', this.stats.totalEstablishments);
        setVal('adminActiveEst', `${this.stats.activeEstablishments} Active (${this.stats.totalHotels} Hotels, ${this.stats.totalCanteens} Canteens)`);
        setVal('adminTotalOrders', this.stats.totalOrders);
        setVal('adminCompletedOrders', `${this.stats.completedOrders} Completed • ${this.stats.cancelledOrders} Cancelled`);

        this.updateTurnoverDisplay();
    },

    updateTurnoverDisplay() {
        const el = document.getElementById('adminTurnoverAmount');
        if (!el || !this.stats) return;

        let amount = this.stats.totalTurnoverThroughWeb;
        let label = 'All-Time Completed Turnover';

        if (this.currentRange === 'today') {
            amount = this.stats.todayTurnover;
            label = "Today's Verified Turnover";
        } else if (this.currentRange === 'week') {
            amount = this.stats.weekTurnover;
            label = 'Past 7 Days Turnover';
        } else if (this.currentRange === 'month') {
            amount = this.stats.monthTurnover;
            label = 'Past 30 Days Turnover';
        }

        el.textContent = `₹${amount.toLocaleString()}`;
        const lblEl = document.getElementById('adminTurnoverLabel');
        if (lblEl) lblEl.textContent = label;
    },

    renderTopEstablishments() {
        const container = document.getElementById('adminTopEstList');
        if (!container || !this.stats.topEstablishments) return;

        container.innerHTML = this.stats.topEstablishments.map((e, idx) => `
            <div style="display:flex; justify-content:space-between; align-items:center; padding:12px 0; border-bottom:1px solid var(--border);">
                <div>
                    <div style="font-size:14px; font-weight:700;">${idx+1}. ${e.name}</div>
                    <div style="font-size:11px; color:var(--text-secondary);">${e.type} • ★ ${e.rating} • ${e.orderCount} orders</div>
                </div>
                <div style="text-align:right;">
                    <div style="font-size:14px; font-weight:800; color:var(--primary);">₹${e.turnover.toLocaleString()}</div>
                    <div style="font-size:10px; color:var(--text-muted);">Platform Sales</div>
                </div>
            </div>
        `).join('') || '<div style="color:var(--text-muted); font-size:12px;">No sales data yet.</div>';
    },

    // ----------------------------------------------------
    // USER MANAGEMENT
    // ----------------------------------------------------
    async loadUsers(search = '') {
        try {
            this.users = await api.getAllUsers(search);
            this.renderUsersTable();
        } catch (e) {
            showToast('Failed to load users: ' + e.message, 'error');
        }
    },

    renderUsersTable() {
        const tbody = document.getElementById('adminUsersTableBody');
        if (!tbody) return;

        tbody.innerHTML = this.users.map(u => {
            const statusColor = u.status === 'ACTIVE' ? 'var(--success)' : (u.status === 'SUSPENDED' ? 'var(--warning)' : 'var(--danger)');
            return `
                <tr>
                    <td><strong>#${u.id}</strong></td>
                    <td>${u.name}</td>
                    <td>${u.email}</td>
                    <td><span class="badge" style="background:var(--bg-surface);">${u.role}</span></td>
                    <td><span style="font-size:12px; font-weight:700; color:${statusColor};">● ${u.status}</span></td>
                    <td>
                        <div style="display:flex; gap:6px;">
                            ${u.status !== 'ACTIVE' ? `<button class="btn btn-secondary btn-sm" onclick="adminApp.setUserStatus(${u.id}, 'ACTIVE')">Activate</button>` : ''}
                            ${u.status !== 'SUSPENDED' ? `<button class="btn btn-secondary btn-sm" style="color:var(--warning);" onclick="adminApp.setUserStatus(${u.id}, 'SUSPENDED')">Suspend</button>` : ''}
                            ${u.status !== 'BLOCKED' ? `<button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="adminApp.setUserStatus(${u.id}, 'BLOCKED')">Block</button>` : ''}
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    },

    async setUserStatus(userId, status) {
        try {
            await api.updateUserStatus(userId, status);
            showToast(`User status updated to ${status}`, 'success');
            await this.loadUsers();
            await this.loadDashboardData();
        } catch (e) {
            showToast('Status update failed: ' + e.message, 'error');
        }
    },

    // ----------------------------------------------------
    // ESTABLISHMENT MANAGEMENT
    // ----------------------------------------------------
    async loadEstablishments() {
        try {
            this.establishments = await api.getEstablishments();
            this.renderEstablishmentsTable();
        } catch (e) {
            showToast('Failed to load establishments: ' + e.message, 'error');
        }
    },

    renderEstablishmentsTable() {
        const tbody = document.getElementById('adminEstTableBody');
        if (!tbody) return;

        tbody.innerHTML = this.establishments.map(e => {
            const isHotel = e.type === 'HOTEL';
            const badgeClass = isHotel ? 'badge-hotel' : 'badge-canteen';
            const statusColor = e.status === 'ACTIVE' ? 'var(--success)' : (e.status === 'SUSPENDED' ? 'var(--warning)' : 'var(--danger)');

            return `
                <tr>
                    <td><strong>#${e.establishmentId}</strong></td>
                    <td>${e.name}</td>
                    <td><span class="badge ${badgeClass}">${e.type}</span></td>
                    <td>${e.city} (${e.locationAddress})</td>
                    <td>★ ${e.rating || 4.2} (${e.reviewCount || 0})</td>
                    <td><span style="font-size:12px; font-weight:700; color:${statusColor};">● ${e.status}</span></td>
                    <td>
                        <div style="display:flex; gap:6px;">
                            ${e.status !== 'ACTIVE' ? `<button class="btn btn-secondary btn-sm" onclick="adminApp.setEstablishmentStatus(${e.establishmentId}, 'ACTIVE')">Approve / Activate</button>` : ''}
                            ${e.status !== 'SUSPENDED' ? `<button class="btn btn-secondary btn-sm" style="color:var(--warning);" onclick="adminApp.setEstablishmentStatus(${e.establishmentId}, 'SUSPENDED')">Suspend</button>` : ''}
                            ${e.status !== 'BLOCKED' ? `<button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="adminApp.setEstablishmentStatus(${e.establishmentId}, 'BLOCKED')">Block</button>` : ''}
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    },

    async setEstablishmentStatus(estId, status) {
        try {
            await api.updateEstablishmentStatus(estId, status);
            showToast(`Establishment status updated to ${status}`, 'success');
            await this.loadEstablishments();
            await this.loadDashboardData();
        } catch (e) {
            showToast('Status update failed: ' + e.message, 'error');
        }
    },

    // ----------------------------------------------------
    // REVIEW MODERATION
    // ----------------------------------------------------
    async loadFlaggedReviews() {
        try {
            this.flaggedReviews = await api.getFlaggedReviews();
            const container = document.getElementById('adminFlaggedReviewsList');
            if (!container) return;

            if (this.flaggedReviews.length === 0) {
                container.innerHTML = '<div style="padding:20px; text-align:center; color:var(--text-muted);">No flagged reviews requiring moderation.</div>';
                return;
            }

            container.innerHTML = this.flaggedReviews.map(r => `
                <div class="card" style="padding:16px; margin-bottom:12px; border-left:4px solid var(--warning);">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                        <div>
                            <strong>${r.customer ? r.customer.name : 'User'}</strong> on 
                            <strong>${r.establishment ? r.establishment.name : 'Establishment'}</strong>
                        </div>
                        <span style="color:#f59e0b; font-weight:700;">★ ${r.rating}</span>
                    </div>
                    <p style="font-size:13px; color:var(--text-secondary); margin-bottom:10px;">"${r.comment}"</p>
                    <div style="display:flex; gap:8px;">
                        <button class="btn btn-secondary btn-sm" onclick="adminApp.unflagReview(${r.reviewId})">Dismiss Report (Keep Review)</button>
                        <button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="adminApp.deleteReview(${r.reviewId})">Delete Inappropriate Review</button>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            console.warn('Flagged reviews load failed:', e);
        }
    },

    async unflagReview(id) {
        try {
            await api.request(`/reviews/${id}/unflag`, { method: 'POST' });
            showToast('Review approved and report dismissed.', 'info');
            await this.loadFlaggedReviews();
        } catch (e) {
            showToast('Action failed: ' + e.message, 'error');
        }
    },

    async deleteReview(id) {
        if (!confirm('Are you sure you want to permanently delete this review?')) return;
        try {
            await api.deleteReview(id);
            showToast('Review permanently deleted.', 'success');
            await this.loadFlaggedReviews();
        } catch (e) {
            showToast('Action failed: ' + e.message, 'error');
        }
    }
};

window.adminApp = adminApp;

document.addEventListener('DOMContentLoaded', () => {
    adminApp.init();
});
