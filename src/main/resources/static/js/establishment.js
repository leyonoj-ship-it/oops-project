// =========================================================
// SMARTDINE — Hotel & Canteen Unified Manager Controller
// HOTEL and CANTEEN share the EXACT same interface!
// =========================================================

const establishmentApp = {
    user: null,
    establishmentId: null,
    establishment: null,
    activeTab: 'dashboard',
    stats: null,
    orders: [],
    menu: [],
    reservations: [],
    reviews: [],
    clearances: [],

    async init() {
        this.user = auth.requireRole('ESTABLISHMENT_OWNER');
        if (!this.user) return;

        this.establishmentId = this.user.establishmentId || 1;
        this.initNavTabs();
        await this.loadEstablishmentInfo();
        await this.loadDashboardData();
    },

    initNavTabs() {
        const navLinks = document.querySelectorAll('.sidebar-nav-item');
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                navLinks.forEach(l => l.classList.remove('active'));
                link.classList.add('active');
                this.activeTab = link.dataset.tab;
                this.switchTab(this.activeTab);
            });
        });
    },

    switchTab(tab) {
        document.querySelectorAll('.tab-section').forEach(sec => sec.style.display = 'none');
        const target = document.getElementById(`tab-${tab}`);
        if (target) target.style.display = 'block';

        if (tab === 'orders') this.loadOrders();
        else if (tab === 'menu') this.loadMenu();
        else if (tab === 'clearance') this.loadClearances();
        else if (tab === 'reservations') this.loadReservations();
        else if (tab === 'reviews') this.loadReviews();
    },

    async loadEstablishmentInfo() {
        try {
            this.establishment = await api.getEstablishmentById(this.establishmentId);
            const nameEl = document.getElementById('estHeaderName');
            const typeEl = document.getElementById('estHeaderType');
            const hoursEl = document.getElementById('estHeaderHours');

            if (nameEl) nameEl.textContent = this.establishment.name;
            if (typeEl) {
                typeEl.textContent = this.establishment.type;
                typeEl.className = `badge ${this.establishment.type === 'HOTEL' ? 'badge-hotel' : 'badge-canteen'}`;
            }
            if (hoursEl) hoursEl.textContent = `Operating Hours: ${this.establishment.openingTime} - ${this.establishment.closingTime}`;
        } catch (e) {
            console.error('Failed to load establishment info:', e);
        }
    },

    async loadDashboardData() {
        try {
            this.stats = await api.getEstablishmentDashboard(this.establishmentId);
            this.renderKPIs();
            this.renderTopFoods();
        } catch (e) {
            console.error('Failed to load dashboard:', e);
        }
    },

    renderKPIs() {
        if (!this.stats) return;
        const setVal = (id, val) => {
            const el = document.getElementById(id);
            if (el) el.textContent = val;
        };

        setVal('kpiTurnover', `₹${this.stats.totalTurnover.toLocaleString()}`);
        setVal('kpiTodayRev', `₹${this.stats.todayRevenue.toLocaleString()}`);
        setVal('kpiTotalOrders', this.stats.totalOrders);
        setVal('kpiTodayOrders', this.stats.todayOrders);
        setVal('kpiPendingOrders', this.stats.pendingOrders);
        setVal('kpiCompletedOrders', this.stats.completedOrders);
        setVal('kpiFastServe', this.stats.activeFastServeOrders);
        setVal('kpiReservations', this.stats.activeReservations);
        setVal('kpiRating', `★ ${this.stats.customerRating} (${this.stats.reviewCount})`);
    },

    renderTopFoods() {
        const topContainer = document.getElementById('topFoodsList');
        const leastContainer = document.getElementById('leastFoodsList');

        if (topContainer && this.stats.topFoodItems) {
            topContainer.innerHTML = this.stats.topFoodItems.map((f, i) => `
                <div style="display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid var(--border);">
                    <div style="font-size:13px; font-weight:600;">${i+1}. ${f.name}</div>
                    <span class="badge" style="background:var(--primary-light); color:var(--primary); font-weight:700;">${f.orderCount} ordered</span>
                </div>
            `).join('') || '<div style="color:var(--text-muted); font-size:12px;">No sales data yet.</div>';
        }

        if (leastContainer && this.stats.leastOrderedFoodItems) {
            leastContainer.innerHTML = this.stats.leastOrderedFoodItems.map((f, i) => `
                <div style="display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid var(--border);">
                    <div style="font-size:13px; color:var(--text-secondary);">${i+1}. ${f.name}</div>
                    <span class="badge" style="background:var(--bg-surface); color:var(--text-secondary);">${f.orderCount} ordered</span>
                </div>
            `).join('') || '<div style="color:var(--text-muted); font-size:12px;">No sales data yet.</div>';
        }
    },

    // ----------------------------------------------------
    // ORDERS PIPELINE MANAGEMENT
    // ----------------------------------------------------
    async loadOrders(filterFastServe = false) {
        try {
            this.orders = filterFastServe 
                ? await api.getFastServeOrders(this.establishmentId)
                : await api.getOrdersByEstablishment(this.establishmentId);
            this.renderOrdersTable();
        } catch (e) {
            showToast('Failed to load orders: ' + e.message, 'error');
        }
    },

    renderOrdersTable() {
        const tbody = document.getElementById('ordersTableBody');
        if (!tbody) return;

        if (this.orders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--text-muted);">No orders found.</td></tr>';
            return;
        }

        tbody.innerHTML = this.orders.map(o => {
            const itemsList = (o.items || []).map(i => `${i.foodName} x${i.quantity}`).join(', ');
            const isFast = o.isFastServe;
            const fastBadge = isFast ? `<span class="badge badge-fast-serve">⚡ Fast Serve #T${o.tableNumber}</span>` : '<span>Dine-In</span>';

            let nextActionBtn = '';
            if (o.status === 'PLACED') {
                nextActionBtn = `<button class="btn btn-primary btn-sm" onclick="establishmentApp.updateOrderStatus(${o.orderId}, 'ACCEPTED')">Accept</button>`;
            } else if (o.status === 'ACCEPTED') {
                nextActionBtn = `<button class="btn btn-primary btn-sm" onclick="establishmentApp.updateOrderStatus(${o.orderId}, 'PREPARING')">Prepare</button>`;
            } else if (o.status === 'PREPARING') {
                nextActionBtn = `<button class="btn btn-primary btn-sm" onclick="establishmentApp.updateOrderStatus(${o.orderId}, 'READY')">Mark Ready</button>`;
            } else if (o.status === 'READY') {
                nextActionBtn = `<button class="btn btn-primary btn-sm" onclick="establishmentApp.updateOrderStatus(${o.orderId}, 'SERVED')">Serve</button>`;
            } else if (o.status === 'SERVED') {
                nextActionBtn = `<button class="btn btn-secondary btn-sm" onclick="establishmentApp.updateOrderStatus(${o.orderId}, 'COMPLETED')">Complete Bill</button>`;
            }

            return `
                <tr>
                    <td><strong>#${o.orderNumber}</strong></td>
                    <td>${fastBadge}</td>
                    <td>${o.customer ? o.customer.name : 'Walk-in'}</td>
                    <td style="max-width:240px; font-size:12px; color:var(--text-secondary);">${itemsList}</td>
                    <td><strong>₹${o.finalAmount}</strong> (${o.paymentMethod})</td>
                    <td><span class="badge" style="background:var(--bg-surface);">${o.status}</span></td>
                    <td>
                        <div style="display:flex; gap:6px;">
                            ${nextActionBtn}
                            ${o.status !== 'COMPLETED' && o.status !== 'CANCELLED' ? 
                                `<button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="establishmentApp.cancelOrder(${o.orderId})">Cancel</button>` : ''}
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    },

    async updateOrderStatus(orderId, newStatus) {
        try {
            await api.updateOrderStatus(orderId, newStatus);
            showToast(`Order status updated to ${newStatus}`, 'success');
            await this.loadOrders();
            await this.loadDashboardData();
        } catch (e) {
            showToast('Failed to update status: ' + e.message, 'error');
        }
    },

    async cancelOrder(orderId) {
        if (!confirm('Are you sure you want to cancel this order?')) return;
        try {
            await api.cancelOrder(orderId, 'Cancelled by manager');
            showToast('Order cancelled', 'info');
            await this.loadOrders();
            await this.loadDashboardData();
        } catch (e) {
            showToast('Failed to cancel order: ' + e.message, 'error');
        }
    },

    // ----------------------------------------------------
    // MENU MANAGEMENT (CRUD)
    // ----------------------------------------------------
    async loadMenu() {
        try {
            this.menu = await api.getMenu(this.establishmentId);
            this.renderMenuGrid();
        } catch (e) {
            showToast('Failed to load menu: ' + e.message, 'error');
        }
    },

    renderMenuGrid() {
        const container = document.getElementById('menuGridContainer');
        if (!container) return;

        container.innerHTML = this.menu.map(item => {
            const vegClass = item.isVeg ? 'badge-veg' : 'badge-non-veg';
            return `
                <div class="card" style="padding:16px;">
                    <div style="display:flex; gap:12px; margin-bottom:10px;">
                        <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=200&q=80'}" 
                             style="width:70px; height:70px; border-radius:var(--radius-md); object-fit:cover;">
                        <div style="flex:1;">
                            <span class="${vegClass}">${item.isVeg ? 'VEG' : 'NON-VEG'}</span>
                            <h4 style="margin:4px 0 2px; font-size:15px;">${item.name}</h4>
                            <div style="font-size:12px; color:var(--text-secondary);">${item.category ? item.category.name : 'Category'}</div>
                        </div>
                    </div>
                    <div style="display:flex; justify-content:space-between; align-items:baseline; margin-bottom:12px;">
                        <div>
                            <span style="font-size:16px; font-weight:800; color:var(--primary);">₹${item.price}</span>
                            ${item.offerPrice ? `<span style="font-size:12px; text-decoration:line-through; color:var(--text-muted); margin-left:4px;">₹${item.offerPrice}</span>` : ''}
                        </div>
                        <span style="font-size:12px; font-weight:600; color:${item.isAvailable ? 'var(--success)' : 'var(--danger)'};">
                            ${item.isAvailable ? '● Available' : '○ Sold Out'}
                        </span>
                    </div>
                    <div style="display:flex; gap:6px; border-top:1px solid var(--border); padding-top:10px;">
                        <button class="btn btn-secondary btn-sm" style="flex:1;" onclick="establishmentApp.toggleAvailability(${item.foodId})">
                            ${item.isAvailable ? 'Mark Sold Out' : 'Mark Available'}
                        </button>
                        <button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="establishmentApp.deleteFood(${item.foodId})">
                            Delete
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    },

    openAddFoodModal() {
        const modal = document.getElementById('foodModal');
        if (!modal) return;
        document.getElementById('foodForm').reset();
        modal.classList.add('active');
    },

    async saveFoodItem(e) {
        e.preventDefault();
        const name = document.getElementById('foodNameInput').value.trim();
        const price = parseFloat(document.getElementById('foodPriceInput').value);
        const categoryName = document.getElementById('foodCatInput').value.trim();
        const isVeg = document.getElementById('foodIsVegInput').value === 'true';
        const imageUrl = document.getElementById('foodImageInput').value.trim();
        const desc = document.getElementById('foodDescInput').value.trim();
        const tags = document.getElementById('foodTagsInput').value.split(',').map(s => s.trim().toUpperCase()).filter(s => s);

        try {
            await api.addFoodItem({
                establishmentId: this.establishmentId,
                name,
                price,
                categoryName,
                isVeg,
                imageUrl,
                description: desc,
                tags
            });

            document.getElementById('foodModal').classList.remove('active');
            showToast('Food item added to menu!', 'success');
            await this.loadMenu();
        } catch (err) {
            showToast('Failed to add food: ' + err.message, 'error');
        }
    },

    async toggleAvailability(foodId) {
        try {
            await api.toggleFoodAvailability(foodId);
            await this.loadMenu();
        } catch (e) {
            showToast('Update failed: ' + e.message, 'error');
        }
    },

    async deleteFood(foodId) {
        if (!confirm('Are you sure you want to delete this menu item?')) return;
        try {
            await api.deleteFoodItem(foodId);
            showToast('Menu item removed', 'info');
            await this.loadMenu();
        } catch (e) {
            showToast('Failed to delete: ' + e.message, 'error');
        }
    },

    // ----------------------------------------------------
    // SMART FOOD CLEARANCE ALERT CREATOR
    // ----------------------------------------------------
    async loadClearances() {
        try {
            this.clearances = await api.getEstablishmentClearances(this.establishmentId);
            this.renderClearanceTable();
            this.populateClearanceFoodDropdown();
        } catch (e) {
            showToast('Failed to load clearance offers: ' + e.message, 'error');
        }
    },

    populateClearanceFoodDropdown() {
        const select = document.getElementById('clearanceFoodSelect');
        if (!select || !this.menu) return;

        select.innerHTML = this.menu.map(f => `
            <option value="${f.foodId}">${f.name} (Original: ₹${f.price})</option>
        `).join('');
    },

    renderClearanceTable() {
        const tbody = document.getElementById('clearanceTableBody');
        if (!tbody) return;

        if (this.clearances.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:20px; color:var(--text-muted);">No active clearance deals. Create one below to minimize closing-time food waste!</td></tr>';
            return;
        }

        tbody.innerHTML = this.clearances.map(c => `
            <tr>
                <td><strong>${c.foodItem ? c.foodItem.name : 'Item'}</strong></td>
                <td><span class="badge badge-clearance">${c.discountPercentage}% OFF</span></td>
                <td><strong>₹${c.clearancePrice}</strong> <span style="text-decoration:line-through; font-size:11px; color:var(--text-muted);">₹${c.originalPrice}</span></td>
                <td>${c.remainingQuantity} / ${c.initialQuantity} remaining</td>
                <td>🕒 ${c.startTime} - ${c.endTime}</td>
                <td>
                    <button class="btn btn-secondary btn-sm" onclick="establishmentApp.expireClearance(${c.offerId})">End Offer</button>
                </td>
            </tr>
        `).join('');
    },

    async createClearanceOffer(e) {
        e.preventDefault();
        const foodId = parseInt(document.getElementById('clearanceFoodSelect').value);
        const discountPercentage = parseInt(document.getElementById('clearanceDiscountInput').value) || 50;
        const quantity = parseInt(document.getElementById('clearanceQtyInput').value) || 10;
        const startTime = document.getElementById('clearanceStartInput').value || '20:30';
        const endTime = document.getElementById('clearanceEndInput').value || '22:30';

        try {
            await api.createClearanceOffer({
                establishmentId: this.establishmentId,
                foodId,
                discountPercentage,
                quantity,
                startTime,
                endTime
            });

            showToast('⚡ Closing-Time Clearance offer published to nearby customers!', 'success');
            await this.loadClearances();
        } catch (err) {
            showToast('Failed to create clearance offer: ' + err.message, 'error');
        }
    },

    async expireClearance(offerId) {
        try {
            await api.request(`/foods/clearance-offers/${offerId}/expire`, { method: 'POST' });
            showToast('Clearance offer ended.', 'info');
            await this.loadClearances();
        } catch (e) {
            showToast('Failed to end offer: ' + e.message, 'error');
        }
    },

    // ----------------------------------------------------
    // RESERVATIONS & REVIEWS
    // ----------------------------------------------------
    async loadReservations() {
        try {
            this.reservations = await api.getReservationsByEstablishment(this.establishmentId);
            const tbody = document.getElementById('reservationsTableBody');
            if (!tbody) return;

            if (this.reservations.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--text-muted);">No reservations found.</td></tr>';
                return;
            }

            tbody.innerHTML = this.reservations.map(r => `
                <tr>
                    <td><strong>#${r.reservationId}</strong></td>
                    <td>${r.customer ? r.customer.name : 'Guest'}</td>
                    <td>${r.reservationDate} at ${r.reservationTime}</td>
                    <td>${r.numberOfGuests} guests</td>
                    <td><span class="badge" style="background:var(--bg-surface);">${r.selectedAmbiance}</span></td>
                    <td>${r.status}</td>
                    <td>
                        <button class="btn btn-secondary btn-sm" onclick="establishmentApp.updateReservationStatus(${r.reservationId}, 'COMPLETED')">Complete</button>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            showToast('Failed to load reservations: ' + e.message, 'error');
        }
    },

    async updateReservationStatus(id, status) {
        try {
            await api.updateReservationStatus(id, status);
            showToast(`Reservation #${id} updated to ${status}`, 'success');
            await this.loadReservations();
        } catch (e) {
            showToast('Update failed: ' + e.message, 'error');
        }
    },

    async loadReviews() {
        try {
            this.reviews = await api.getReviews(this.establishmentId);
            const container = document.getElementById('reviewsContainer');
            if (!container) return;

            if (this.reviews.length === 0) {
                container.innerHTML = '<div style="padding:20px; text-align:center; color:var(--text-muted);">No reviews yet.</div>';
                return;
            }

            container.innerHTML = this.reviews.map(r => `
                <div class="card" style="padding:16px; margin-bottom:12px;">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                        <strong>${r.customer ? r.customer.name : 'Anonymous'}</strong>
                        <span style="color:#f59e0b; font-weight:700;">★ ${r.rating}</span>
                    </div>
                    <p style="font-size:13px; color:var(--text-secondary); margin-bottom:8px;">"${r.comment}"</p>
                    <div style="display:flex; justify-content:space-between; align-items:center; font-size:11px; color:var(--text-muted);">
                        <span>${r.createdAt ? r.createdAt.substring(0, 10) : ''}</span>
                        ${!r.isFlagged ? 
                            `<button style="background:none; border:none; color:var(--danger); cursor:pointer; font-size:11px;" 
                                     onclick="establishmentApp.flagReview(${r.reviewId})">Report to Admin for Moderation</button>` :
                            '<span style="color:var(--warning);">Reported for Review</span>'}
                    </div>
                </div>
            `).join('');
        } catch (e) {
            showToast('Failed to load reviews: ' + e.message, 'error');
        }
    },

    async flagReview(id) {
        try {
            await api.flagReview(id);
            showToast('Review reported to Admin moderation queue.', 'info');
            await this.loadReviews();
        } catch (e) {
            showToast('Reporting failed: ' + e.message, 'error');
        }
    }
};

window.establishmentApp = establishmentApp;

document.addEventListener('DOMContentLoaded', () => {
    establishmentApp.init();
});
