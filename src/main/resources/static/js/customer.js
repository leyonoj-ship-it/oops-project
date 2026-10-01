// =========================================================
// SMARTDINE — Customer Experience Controller
// =========================================================

const customerApp = {
    user: null,
    establishments: [],
    currentAmbiance: 'DEFAULT',
    activeTab: 'ALL', // ALL, HOTEL, CANTEEN, CLEARANCE
    selectedEstablishment: null,
    menuItems: [],
    cart: [],
    activeOrders: [],
    activeClearances: [],

    async init() {
        this.user = auth.requireRole('CUSTOMER');
        if (!this.user) return;

        this.renderUserBadge();
        this.initAmbianceSelector();
        this.initSearch();
        this.initTabs();
        this.initCart();

        // Load location & nearby establishments
        await this.loadNearbyEstablishments();
        await this.loadActiveClearances();
        await this.loadCustomerOrders();
    },

    renderUserBadge() {
        const nameEl = document.getElementById('userNameBadge');
        if (nameEl && this.user) {
            nameEl.textContent = this.user.name;
        }
    },

    // ----------------------------------------------------
    // AMBIANCE DYNAMIC THEME & RECOMMENDATION SYSTEM
    // ----------------------------------------------------
    initAmbianceSelector() {
        const select = document.getElementById('ambianceSelect');
        if (!select) return;

        select.addEventListener('change', (e) => {
            this.setAmbiance(e.target.value);
        });
    },

    async setAmbiance(ambiance) {
        this.currentAmbiance = ambiance;
        document.documentElement.setAttribute('data-theme', ambiance);

        // Update active dropdown
        const select = document.getElementById('ambianceSelect');
        if (select) select.value = ambiance;

        const banner = document.getElementById('ambianceRecommendationBanner');
        if (ambiance === 'DEFAULT') {
            if (banner) banner.style.display = 'none';
            return;
        }

        // Fetch recommendations from backend
        try {
            const recommendedFoods = await api.getFoodRecommendations(ambiance);
            this.renderAmbianceBanner(ambiance, recommendedFoods);
        } catch (err) {
            console.error('Failed to load ambiance recommendations:', err);
        }
    },

    renderAmbianceBanner(ambiance, foods) {
        const banner = document.getElementById('ambianceRecommendationBanner');
        if (!banner) return;

        const ambianceNames = {
            'CANDLE_LIGHT': 'Candle Light Dinner 🕯️',
            'ROMANTIC': 'Romantic Dining ❤️',
            'FAMILY': 'Family Feast 👨‍👩‍👧‍👦',
            'PREMIUM': 'Premium Luxury 🥂',
            'CASUAL': 'Casual Hangout 🍕',
            'FRIENDS': 'Friends Gathering 🍔',
            'OUTDOOR': 'Outdoor Garden 🌿',
            'BIRTHDAY': 'Birthday Celebration 🎂',
            'QUIET_DINING': 'Quiet & Peaceful ☕'
        };

        const title = ambianceNames[ambiance] || ambiance;

        let foodChips = '';
        if (foods && foods.length > 0) {
            foodChips = foods.slice(0, 4).map(f => `
                <div class="ambiance-food-chip" onclick="customerApp.quickAddRecommendedFood(${f.foodId}, '${f.name.replace(/'/g, "\\'")}', ${f.price})">
                    <span>+ Add ${f.name} (₹${f.price})</span>
                </div>
            `).join('');
        }

        banner.innerHTML = `
            <div class="ambiance-banner-content">
                <div>
                    <span class="badge badge-fast-serve" style="margin-bottom:6px;">Ambiance Active</span>
                    <h3 style="color:var(--text-primary); margin-bottom:4px;">${title}</h3>
                    <p style="font-size:13px; color:var(--text-secondary); margin-bottom:10px;">
                        Curated atmosphere styling applied. Discover recommended dishes for this mood:
                    </p>
                    <div style="display:flex; flex-wrap:wrap; gap:8px;">
                        ${foodChips}
                    </div>
                </div>
                <div style="display:flex; gap:10px; align-items:center;">
                    <button class="btn btn-primary btn-sm" onclick="customerApp.openReservationModalWithAmbiance('${ambiance}')">
                        Reserve Table for this Ambiance
                    </button>
                    <button class="btn btn-secondary btn-sm" onclick="customerApp.setAmbiance('DEFAULT')">
                        Reset Theme
                    </button>
                </div>
            </div>
        `;
        banner.style.display = 'block';
    },

    quickAddRecommendedFood(foodId, name, price) {
        this.addToCart({
            foodId,
            name,
            price,
            quantity: 1,
            customization: `Recommended for ${this.currentAmbiance}`
        });
        showToast(`Added ${name} to cart!`, 'success');
    },

    // ----------------------------------------------------
    // NEARBY ESTABLISHMENTS & SEARCH
    // ----------------------------------------------------
    async loadNearbyEstablishments() {
        const grid = document.getElementById('establishmentsGrid');
        if (grid) grid.innerHTML = '<div style="padding:40px; text-align:center; color:var(--text-secondary);">Loading nearby hotels & canteens...</div>';

        try {
            const loc = await smartMaps.requestUserLocation();
            this.establishments = await api.getNearbyEstablishments(loc.lat, loc.lon, 35, null);
            this.renderEstablishments();
            smartMaps.initMap('mapContainer', this.establishments, (id) => this.openEstablishment(id));
        } catch (error) {
            console.error('Error fetching establishments:', error);
            if (grid) grid.innerHTML = `<div style="padding:40px; text-align:center; color:var(--danger);">Failed to load establishments: ${error.message}</div>`;
        }
    },

    async loadActiveClearances() {
        try {
            this.activeClearances = await api.getActiveClearances();
            this.renderClearanceSection();
        } catch (e) {
            console.warn('Clearance offers load failed:', e);
        }
    },

    renderClearanceSection() {
        const container = document.getElementById('clearanceSection');
        if (!container) return;

        if (!this.activeClearances || this.activeClearances.length === 0) {
            container.style.display = 'none';
            return;
        }

        container.style.display = 'block';
        const listEl = document.getElementById('clearanceList');
        if (!listEl) return;

        listEl.innerHTML = this.activeClearances.map(c => `
            <div class="card" style="padding:16px; border-left:4px solid var(--danger);">
                <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                    <div>
                        <span class="badge badge-clearance">⚡ Closing-Time Clearance: ${c.discountPercentage}% OFF</span>
                        <h4 style="margin:6px 0 2px;">${c.foodItem ? c.foodItem.name : 'Clearance Food'}</h4>
                        <div style="font-size:12px; color:var(--text-secondary); margin-bottom:8px;">
                            ${c.establishment ? c.establishment.name : ''} • Ends at ${c.endTime}
                        </div>
                        <div style="display:flex; align-items:baseline; gap:8px;">
                            <span style="font-size:18px; font-weight:800; color:var(--danger);">₹${c.clearancePrice}</span>
                            <span style="font-size:13px; text-decoration:line-through; color:var(--text-muted);">₹${c.originalPrice}</span>
                            <span style="font-size:12px; font-weight:600; color:var(--warning);">Only ${c.remainingQuantity} portions left!</span>
                        </div>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="customerApp.openEstablishment(${c.establishment.establishmentId})">
                        Grab Deal
                    </button>
                </div>
            </div>
        `).join('');
    },

    renderEstablishments() {
        const grid = document.getElementById('establishmentsGrid');
        if (!grid) return;

        let filtered = this.establishments;
        if (this.activeTab === 'HOTEL') {
            filtered = filtered.filter(e => e.type === 'HOTEL');
        } else if (this.activeTab === 'CANTEEN') {
            filtered = filtered.filter(e => e.type === 'CANTEEN');
        }

        if (filtered.length === 0) {
            grid.innerHTML = '<div style="padding:40px; text-align:center; color:var(--text-secondary); grid-column:1/-1;">No establishments found matching your filter.</div>';
            return;
        }

        grid.innerHTML = filtered.map(est => {
            const isHotel = est.type === 'HOTEL';
            const badgeClass = isHotel ? 'badge-hotel' : 'badge-canteen';
            const iconEmoji = isHotel ? '🏨 Hotel & Restaurant' : '☕ Campus Canteen';
            const distanceText = est.distanceKm ? `📍 ${est.distanceKm} km away` : '📍 Nearby';

            const ambiancesHtml = (est.supportedAmbiances || []).slice(0, 3).map(a => 
                `<span style="font-size:10px; background:var(--bg-surface); padding:2px 8px; border-radius:10px; color:var(--text-secondary);">${a}</span>`
            ).join(' ');

            return `
                <div class="card" onclick="customerApp.openEstablishment(${est.establishmentId})" style="cursor:pointer;">
                    <div style="position:relative; height:180px; overflow:hidden;">
                        <img src="${est.coverImageUrl || 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80'}" 
                             alt="${est.name}" style="width:100%; height:100%; object-fit:cover; transition:transform 0.4s ease;" 
                             onmouseover="this.style.transform='scale(1.05)'" onmouseout="this.style.transform='scale(1)'">
                        <div style="position:absolute; top:12px; left:12px;">
                            <span class="badge ${badgeClass}">${iconEmoji}</span>
                        </div>
                        <div style="position:absolute; top:12px; right:12px; background:rgba(0,0,0,0.7); color:#f59e0b; padding:4px 8px; border-radius:12px; font-size:12px; font-weight:bold;">
                            ★ ${est.rating || 4.2} <span style="font-size:10px; color:#cbd5e1;">(${est.reviewCount || 0})</span>
                        </div>
                    </div>
                    <div style="padding:18px;">
                        <h3 style="font-size:18px; margin-bottom:6px; color:var(--text-primary);">${est.name}</h3>
                        <p style="font-size:12px; color:var(--text-secondary); margin-bottom:12px; line-height:1.4;">
                            ${distanceText} • ${est.locationAddress}
                        </p>
                        <div style="display:flex; flex-wrap:wrap; gap:4px; margin-bottom:14px;">
                            ${ambiancesHtml}
                        </div>
                        <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px solid var(--border); padding-top:12px;">
                            <span style="font-size:12px; color:var(--text-muted);">🕒 ${est.openingTime} - ${est.closingTime}</span>
                            <div style="display:flex; gap:8px;">
                                <button class="btn btn-outline btn-sm" onclick="event.stopPropagation(); customerApp.openReservationModal(${est.establishmentId})">
                                    Reserve
                                </button>
                                <button class="btn btn-primary btn-sm">
                                    View Menu
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    initSearch() {
        const searchInput = document.getElementById('globalSearch');
        if (!searchInput) return;

        let debounceTimer;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(async () => {
                const query = e.target.value.trim();
                if (query.length === 0) {
                    await this.loadNearbyEstablishments();
                } else {
                    const results = await api.searchEstablishments(query);
                    this.establishments = results;
                    this.renderEstablishments();
                }
            }, 300);
        });
    },

    initTabs() {
        const tabs = document.querySelectorAll('.filter-tab');
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                this.activeTab = tab.dataset.tab;
                this.renderEstablishments();
            });
        });
    },

    // ----------------------------------------------------
    // ESTABLISHMENT DETAILS & MENU
    // ----------------------------------------------------
    async openEstablishment(establishmentId) {
        try {
            this.selectedEstablishment = await api.getEstablishmentById(establishmentId);
            this.menuItems = await api.getMenu(establishmentId);
            this.renderEstablishmentModal();
        } catch (err) {
            showToast('Failed to load menu: ' + err.message, 'error');
        }
    },

    renderEstablishmentModal() {
        const modal = document.getElementById('establishmentModal');
        if (!modal || !this.selectedEstablishment) return;

        const est = this.selectedEstablishment;
        const isHotel = est.type === 'HOTEL';
        const badgeClass = isHotel ? 'badge-hotel' : 'badge-canteen';

        document.getElementById('modalEstCover').src = est.coverImageUrl || 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80';
        document.getElementById('modalEstName').textContent = est.name;
        document.getElementById('modalEstType').className = `badge ${badgeClass}`;
        document.getElementById('modalEstType').textContent = est.type;
        document.getElementById('modalEstRating').textContent = `★ ${est.rating || 4.2} (${est.reviewCount || 0} reviews)`;
        document.getElementById('modalEstLocation').textContent = `📍 ${est.locationAddress}, ${est.city} • Open: ${est.openingTime} - ${est.closingTime}`;
        document.getElementById('modalEstDesc').textContent = est.description || '';

        // Render Ambiances
        const ambiancesEl = document.getElementById('modalEstAmbiances');
        if (ambiancesEl) {
            ambiancesEl.innerHTML = (est.supportedAmbiances || []).map(a => 
                `<span style="font-size:11px; background:var(--bg-surface); padding:4px 10px; border-radius:12px; font-weight:600;">${a}</span>`
            ).join(' ');
        }

        // Render Menu Items
        this.renderMenuItems();

        modal.classList.add('active');
    },

    renderMenuItems() {
        const container = document.getElementById('modalMenuContainer');
        if (!container) return;

        if (this.menuItems.length === 0) {
            container.innerHTML = '<div style="padding:30px; text-align:center; color:var(--text-secondary);">No menu items currently listed.</div>';
            return;
        }

        // Group by category
        const groups = {};
        this.menuItems.forEach(item => {
            const cat = item.category ? item.category.name : 'Chef Specials';
            if (!groups[cat]) groups[cat] = [];
            groups[cat].push(item);
        });

        let html = '';
        for (const [catName, items] of Object.entries(groups)) {
            html += `<h4 style="margin:24px 0 12px; color:var(--primary); font-size:16px; border-bottom:1px solid var(--border); padding-bottom:6px;">${catName}</h4>`;
            html += `<div class="grid grid-cols-2">`;
            items.forEach(item => {
                const vegClass = item.isVeg ? 'badge-veg' : 'badge-non-veg';
                const vegText = item.isVeg ? 'VEG' : 'NON-VEG';
                const effectivePrice = (item.clearancePrice && item.clearancePrice > 0) ? item.clearancePrice :
                                       (item.offerPrice && item.offerPrice > 0) ? item.offerPrice : item.price;
                const hasDiscount = effectivePrice < item.price;
                const isClearance = item.clearancePrice && item.clearancePrice > 0;

                const clearanceBadge = isClearance ? '<span class="badge badge-clearance" style="font-size:9px;">50% Clearance Offer</span>' : '';
                const offerBadge = (!isClearance && hasDiscount) ? `<span class="badge" style="background:#fef3c7; color:#b45309; font-size:9px;">${Math.round(((item.price - effectivePrice)/item.price)*100)}% OFF</span>` : '';

                html += `
                    <div class="card" style="padding:14px; display:flex; gap:14px; align-items:center;">
                        <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=200&q=80'}" 
                             style="width:80px; height:80px; object-fit:cover; border-radius:var(--radius-md); flex-shrink:0;">
                        <div style="flex:1;">
                            <div style="display:flex; align-items:center; gap:6px; margin-bottom:4px;">
                                <span class="${vegClass}">${vegText}</span>
                                ${clearanceBadge}
                                ${offerBadge}
                            </div>
                            <h5 style="font-size:15px; margin-bottom:4px;">${item.name}</h5>
                            <p style="font-size:12px; color:var(--text-secondary); margin-bottom:8px; line-height:1.3;">
                                ${(item.description || '').substring(0, 60)}...
                            </p>
                            <div style="display:flex; justify-content:space-between; align-items:center;">
                                <div>
                                    <span style="font-size:16px; font-weight:800; color:var(--primary);">₹${effectivePrice}</span>
                                    ${hasDiscount ? `<span style="font-size:12px; text-decoration:line-through; color:var(--text-muted); margin-left:4px;">₹${item.price}</span>` : ''}
                                </div>
                                <button class="btn btn-primary btn-sm" onclick="customerApp.openCustomizeModal(${item.foodId})">
                                    + Add
                                </button>
                            </div>
                        </div>
                    </div>
                `;
            });
            html += `</div>`;
        }

        container.innerHTML = html;
    },

    // ----------------------------------------------------
    // FOOD CUSTOMIZATION MODAL
    // ----------------------------------------------------
    openCustomizeModal(foodId) {
        const item = this.menuItems.find(m => m.foodId === foodId);
        if (!item) return;

        const effectivePrice = (item.clearancePrice && item.clearancePrice > 0) ? item.clearancePrice :
                               (item.offerPrice && item.offerPrice > 0) ? item.offerPrice : item.price;

        const modal = document.getElementById('customizeModal');
        if (!modal) return;

        document.getElementById('customFoodName').textContent = item.name;
        document.getElementById('customFoodPrice').textContent = `₹${effectivePrice}`;
        document.getElementById('customFoodId').value = foodId;
        document.getElementById('customFoodQty').value = 1;

        // Reset customization checkboxes
        document.querySelectorAll('.custom-pref-checkbox').forEach(cb => cb.checked = false);

        modal.classList.add('active');
    },

    confirmAddToCart() {
        const foodId = parseInt(document.getElementById('customFoodId').value);
        const qty = parseInt(document.getElementById('customFoodQty').value) || 1;
        const item = this.menuItems.find(m => m.foodId === foodId);
        if (!item) return;

        const effectivePrice = (item.clearancePrice && item.clearancePrice > 0) ? item.clearancePrice :
                               (item.offerPrice && item.offerPrice > 0) ? item.offerPrice : item.price;

        const checkedPrefs = Array.from(document.querySelectorAll('.custom-pref-checkbox:checked')).map(cb => cb.value);
        const customNote = document.getElementById('customNoteInput')?.value.trim();
        if (customNote) checkedPrefs.push(customNote);

        const customization = checkedPrefs.join(', ');

        this.addToCart({
            foodId: item.foodId,
            name: item.name,
            price: effectivePrice,
            quantity: qty,
            customization: customization || 'Standard preparation',
            establishmentId: this.selectedEstablishment.establishmentId
        });

        document.getElementById('customizeModal').classList.remove('active');
        showToast(`Added ${item.name} (x${qty}) to cart!`, 'success');
    },

    // ----------------------------------------------------
    // CART & CHECKOUT
    // ----------------------------------------------------
    initCart() {
        this.updateCartBadge();
    },

    addToCart(item) {
        // Enforce same establishment or prompt
        if (this.cart.length > 0 && this.cart[0].establishmentId && item.establishmentId && this.cart[0].establishmentId !== item.establishmentId) {
            if (confirm('Your cart contains items from another establishment. Clear cart and add this item?')) {
                this.cart = [];
            } else {
                return;
            }
        }

        const existing = this.cart.find(c => c.foodId === item.foodId && c.customization === item.customization);
        if (existing) {
            existing.quantity += item.quantity;
        } else {
            this.cart.push({ ...item });
        }

        this.updateCartBadge();
        this.renderCartDrawer();
    },

    updateCartBadge() {
        const count = this.cart.reduce((sum, item) => sum + item.quantity, 0);
        const badge = document.getElementById('cartBadge');
        if (badge) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline-flex' : 'none';
        }
    },

    toggleCartDrawer(open = null) {
        const drawer = document.getElementById('cartDrawer');
        if (!drawer) return;
        if (open === null) {
            drawer.classList.toggle('open');
        } else if (open) {
            drawer.classList.add('open');
            this.renderCartDrawer();
        } else {
            drawer.classList.remove('open');
        }
    },

    renderCartDrawer() {
        const container = document.getElementById('cartItemsList');
        if (!container) return;

        if (this.cart.length === 0) {
            container.innerHTML = `
                <div style="padding:40px 20px; text-align:center; color:var(--text-secondary);">
                    <div style="font-size:40px; margin-bottom:12px;">🛒</div>
                    <h4>Your Cart is Empty</h4>
                    <p style="font-size:13px; margin-top:4px;">Browse delicious foods from hotels & canteens near you.</p>
                </div>
            `;
            this.updateCartTotals(0, 0, 0);
            return;
        }

        let subtotal = 0;
        container.innerHTML = this.cart.map((item, idx) => {
            const itemTotal = item.price * item.quantity;
            subtotal += itemTotal;
            return `
                <div style="display:flex; justify-content:space-between; align-items:center; padding:12px 0; border-bottom:1px solid var(--border);">
                    <div style="flex:1;">
                        <h5 style="font-size:14px; margin-bottom:2px;">${item.name}</h5>
                        <div style="font-size:11px; color:var(--text-secondary); margin-bottom:4px;">
                            ₹${item.price} each • ${item.customization}
                        </div>
                        <div style="display:flex; align-items:center; gap:8px;">
                            <button class="btn btn-secondary btn-sm" style="padding:2px 8px;" onclick="customerApp.changeCartQty(${idx}, -1)">-</button>
                            <span style="font-size:13px; font-weight:700;">${item.quantity}</span>
                            <button class="btn btn-secondary btn-sm" style="padding:2px 8px;" onclick="customerApp.changeCartQty(${idx}, 1)">+</button>
                        </div>
                    </div>
                    <div style="text-align:right;">
                        <div style="font-weight:700; color:var(--primary); font-size:15px;">₹${itemTotal.toFixed(2)}</div>
                        <button style="background:none; border:none; color:var(--danger); font-size:11px; cursor:pointer; margin-top:4px;" 
                                onclick="customerApp.removeCartItem(${idx})">Remove</button>
                    </div>
                </div>
            `;
        }).join('');

        const tax = Math.round((subtotal * 0.05) * 100) / 100; // 5% GST
        const finalAmount = Math.round((subtotal + tax) * 100) / 100;
        this.updateCartTotals(subtotal, tax, finalAmount);
    },

    changeCartQty(idx, delta) {
        if (!this.cart[idx]) return;
        this.cart[idx].quantity += delta;
        if (this.cart[idx].quantity <= 0) {
            this.cart.splice(idx, 1);
        }
        this.updateCartBadge();
        this.renderCartDrawer();
    },

    removeCartItem(idx) {
        this.cart.splice(idx, 1);
        this.updateCartBadge();
        this.renderCartDrawer();
    },

    updateCartTotals(subtotal, tax, finalAmount) {
        const subtotalEl = document.getElementById('cartSubtotal');
        const taxEl = document.getElementById('cartTax');
        const finalEl = document.getElementById('cartFinal');

        if (subtotalEl) subtotalEl.textContent = `₹${subtotal.toFixed(2)}`;
        if (taxEl) taxEl.textContent = `₹${tax.toFixed(2)}`;
        if (finalEl) finalEl.textContent = `₹${finalAmount.toFixed(2)}`;
    },

    async checkoutCart() {
        if (this.cart.length === 0) {
            showToast('Your cart is empty!', 'error');
            return;
        }

        const paymentMethod = document.querySelector('input[name="cartPaymentMethod"]:checked')?.value || 'UPI';
        const upiVpa = document.getElementById('cartUpiVpa')?.value.trim() || 'customer@okhdfcbank';
        const estId = this.cart[0].establishmentId || (this.selectedEstablishment ? this.selectedEstablishment.establishmentId : this.establishments[0].establishmentId);

        try {
            const orderPayload = {
                customerId: this.user.userId,
                establishmentId: estId,
                isFastServe: false,
                paymentMethod: paymentMethod,
                paymentReference: paymentMethod === 'UPI' ? upiVpa : 'Counter Cash Pickup',
                items: this.cart.map(i => ({
                    foodId: i.foodId,
                    quantity: i.quantity,
                    customization: i.customization
                }))
            };

            const order = await api.placeOrder(orderPayload);
            this.cart = [];
            this.updateCartBadge();
            this.toggleCartDrawer(false);
            showToast(`Order #${order.orderNumber} placed successfully!`, 'success');
            this.openOrderTracker(order.orderId);
            this.loadCustomerOrders();
        } catch (error) {
            showToast('Checkout failed: ' + error.message, 'error');
        }
    },

    // ----------------------------------------------------
    // FAST SERVE FEATURE (CRITICAL SMARTDINE INNOVATION)
    // ----------------------------------------------------
    openFastServeModal() {
        const modal = document.getElementById('fastServeModal');
        if (!modal) return;

        // Populate establishment dropdown
        const estSelect = document.getElementById('fastServeEstSelect');
        if (estSelect) {
            estSelect.innerHTML = this.establishments.map(e => `
                <option value="${e.establishmentId}">${e.name} (${e.type})</option>
            `).join('');
            if (this.selectedEstablishment) {
                estSelect.value = this.selectedEstablishment.establishmentId;
            }
        }

        this.onFastServeEstablishmentChanged();
        modal.classList.add('active');
    },

    async onFastServeEstablishmentChanged() {
        const estSelect = document.getElementById('fastServeEstSelect');
        const itemsContainer = document.getElementById('fastServeItemsList');
        if (!estSelect || !itemsContainer) return;

        const estId = parseInt(estSelect.value);
        try {
            const foods = await api.getMenu(estId);
            itemsContainer.innerHTML = foods.slice(0, 6).map(f => `
                <div style="display:flex; justify-content:space-between; align-items:center; padding:8px; border:1px solid var(--border); border-radius:var(--radius-md); margin-bottom:6px;">
                    <div>
                        <div style="font-size:13px; font-weight:700;">${f.name}</div>
                        <div style="font-size:11px; color:var(--text-secondary);">₹${f.price}</div>
                    </div>
                    <button class="btn btn-outline btn-sm" onclick="customerApp.quickFastServeAdd(${f.foodId}, '${f.name.replace(/'/g, "\\'")}', ${f.price})">
                        + Select
                    </button>
                </div>
            `).join('');
        } catch (e) {
            itemsContainer.innerHTML = '<div style="font-size:12px; color:var(--text-muted);">Failed to load menu.</div>';
        }
    },

    quickFastServeAdd(foodId, name, price) {
        const estId = parseInt(document.getElementById('fastServeEstSelect').value);
        this.addToCart({
            foodId,
            name,
            price,
            quantity: 1,
            customization: 'Fast Serve - Table Express',
            establishmentId: estId
        });
        showToast(`Added ${name} for Fast Serve!`, 'fast-serve');
    },

    async submitFastServeOrder() {
        const tableNumber = parseInt(document.getElementById('fastServeTableInput').value);
        const estId = parseInt(document.getElementById('fastServeEstSelect').value);

        if (!tableNumber || tableNumber <= 0) {
            showToast('Please enter your valid Table Number!', 'error');
            return;
        }

        if (this.cart.length === 0) {
            showToast('Please select at least one food item for your table!', 'error');
            return;
        }

        const paymentMethod = document.querySelector('input[name="fastServePayment"]:checked')?.value || 'UPI';
        const upiVpa = document.getElementById('fastServeUpiVpa')?.value.trim() || 'table' + tableNumber + '@upi';

        try {
            const payload = {
                customerId: this.user.userId,
                establishmentId: estId,
                isFastServe: true,
                tableNumber: tableNumber,
                paymentMethod: paymentMethod,
                paymentReference: paymentMethod === 'UPI' ? upiVpa : 'Cash at Table #' + tableNumber,
                items: this.cart.map(i => ({
                    foodId: i.foodId,
                    quantity: i.quantity,
                    customization: i.customization || 'Fast Serve'
                }))
            };

            const order = await api.placeOrder(payload);
            this.cart = [];
            this.updateCartBadge();
            document.getElementById('fastServeModal').classList.remove('active');
            showToast(`⚡ FAST SERVE Order #${order.orderNumber} for Table ${tableNumber} placed!`, 'success');
            this.openOrderTracker(order.orderId);
            this.loadCustomerOrders();
        } catch (err) {
            showToast('Fast Serve failed: ' + err.message, 'error');
        }
    },

    // ----------------------------------------------------
    // TABLE RESERVATIONS
    // ----------------------------------------------------
    openReservationModal(establishmentId) {
        const est = this.establishments.find(e => e.establishmentId === establishmentId);
        if (!est) return;

        const modal = document.getElementById('reservationModal');
        if (!modal) return;

        document.getElementById('resEstId').value = establishmentId;
        document.getElementById('resEstName').textContent = est.name;

        // Default to tomorrow's date
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('resDate').value = tomorrow.toISOString().split('T')[0];

        modal.classList.add('active');
    },

    openReservationModalWithAmbiance(ambiance) {
        if (!this.selectedEstablishment && this.establishments.length > 0) {
            this.selectedEstablishment = this.establishments[0];
        }
        if (this.selectedEstablishment) {
            this.openReservationModal(this.selectedEstablishment.establishmentId);
            const ambianceSelect = document.getElementById('resAmbiance');
            if (ambianceSelect) ambianceSelect.value = ambiance;
        }
    },

    async submitReservation() {
        const establishmentId = parseInt(document.getElementById('resEstId').value);
        const reservationDate = document.getElementById('resDate').value;
        const reservationTime = document.getElementById('resTime').value;
        const numberOfGuests = parseInt(document.getElementById('resGuests').value) || 2;
        const selectedAmbiance = document.getElementById('resAmbiance').value;
        const tableNumber = parseInt(document.getElementById('resTableNum').value) || null;
        const specialNotes = document.getElementById('resNotes').value;

        try {
            const res = await api.createReservation({
                customerId: this.user.userId,
                establishmentId,
                reservationDate,
                reservationTime,
                numberOfGuests,
                selectedAmbiance,
                tableNumber,
                specialNotes
            });

            document.getElementById('reservationModal').classList.remove('active');
            showToast(`Table reserved successfully for ${numberOfGuests} guests! (Ref #${res.reservationId})`, 'success');
        } catch (e) {
            showToast('Reservation error: ' + e.message, 'error');
        }
    },

    // ----------------------------------------------------
    // LIVE ORDER TRACKER
    // ----------------------------------------------------
    async openOrderTracker(orderId) {
        try {
            const order = await api.getOrder(orderId);
            const modal = document.getElementById('orderTrackerModal');
            if (!modal) return;

            document.getElementById('trackOrderNumber').textContent = order.orderNumber;
            document.getElementById('trackEstName').textContent = order.establishment ? order.establishment.name : '';
            document.getElementById('trackOrderType').textContent = order.isFastServe ? `⚡ FAST SERVE (Table #${order.tableNumber})` : 'Dine-In / Regular Order';
            document.getElementById('trackFinalAmount').textContent = `₹${order.finalAmount}`;
            document.getElementById('trackPaymentMethod').textContent = `${order.paymentMethod} (${order.paymentStatus})`;

            // Steps status visualization
            const steps = ['PLACED', 'ACCEPTED', 'PREPARING', 'READY', 'SERVED', 'COMPLETED'];
            const currentIdx = steps.indexOf(order.status);

            const stepsContainer = document.getElementById('trackStepsContainer');
            if (stepsContainer) {
                stepsContainer.innerHTML = steps.map((step, idx) => {
                    const isDone = idx <= currentIdx;
                    const isCurrent = idx === currentIdx;
                    const color = isDone ? 'var(--primary)' : 'var(--border)';
                    const textColor = isDone ? 'var(--text-primary)' : 'var(--text-muted)';
                    return `
                        <div style="flex:1; text-align:center;">
                            <div style="width:28px; height:28px; border-radius:50%; background:${color}; color:white; display:flex; align-items:center; justify-content:center; margin:0 auto 6px; font-size:12px; font-weight:bold;">
                                ${isDone ? '✓' : idx + 1}
                            </div>
                            <div style="font-size:11px; font-weight:${isCurrent ? '700' : '500'}; color:${textColor};">${step}</div>
                        </div>
                    `;
                }).join('');
            }

            modal.classList.add('active');
        } catch (e) {
            showToast('Failed to load order: ' + e.message, 'error');
        }
    },

    async loadCustomerOrders() {
        try {
            this.activeOrders = await api.getOrdersByCustomer(this.user.userId);
            this.renderOrdersHistory();
        } catch (e) {
            console.warn('Orders history load failed:', e);
        }
    },

    renderOrdersHistory() {
        const container = document.getElementById('ordersHistoryList');
        if (!container) return;

        if (this.activeOrders.length === 0) {
            container.innerHTML = '<div style="padding:20px; text-align:center; color:var(--text-secondary);">No past orders found.</div>';
            return;
        }

        container.innerHTML = this.activeOrders.slice(0, 5).map(o => `
            <div class="card" style="padding:14px; margin-bottom:10px;">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <div style="display:flex; align-items:center; gap:8px;">
                            <span style="font-weight:700; font-size:14px;">#${o.orderNumber}</span>
                            ${o.isFastServe ? '<span class="badge badge-fast-serve">⚡ Fast Serve</span>' : ''}
                            <span class="badge" style="background:var(--bg-surface);">${o.status}</span>
                        </div>
                        <div style="font-size:12px; color:var(--text-secondary); margin-top:2px;">
                            ${o.establishment ? o.establishment.name : ''} • ₹${o.finalAmount}
                        </div>
                    </div>
                    <div style="display:flex; gap:8px;">
                        <button class="btn btn-outline btn-sm" onclick="customerApp.openOrderTracker(${o.orderId})">Track</button>
                        ${o.status === 'COMPLETED' ? `<button class="btn btn-secondary btn-sm" onclick="customerApp.openReviewModal(${o.establishment.establishmentId}, ${o.orderId})">★ Review</button>` : ''}
                    </div>
                </div>
            </div>
        `).join('');
    },

    // ----------------------------------------------------
    // REVIEW SUBMISSION
    // ----------------------------------------------------
    openReviewModal(establishmentId, orderId = null) {
        const modal = document.getElementById('reviewModal');
        if (!modal) return;

        document.getElementById('reviewEstId').value = establishmentId;
        document.getElementById('reviewOrderId').value = orderId || '';
        document.getElementById('reviewRating').value = 5.0;
        document.getElementById('reviewComment').value = '';

        modal.classList.add('active');
    },

    async submitReview() {
        const establishmentId = parseInt(document.getElementById('reviewEstId').value);
        const orderIdVal = document.getElementById('reviewOrderId').value;
        const rating = parseFloat(document.getElementById('reviewRating').value);
        const comment = document.getElementById('reviewComment').value.trim();

        try {
            await api.submitReview({
                customerId: this.user.userId,
                establishmentId,
                orderId: orderIdVal ? parseInt(orderIdVal) : null,
                rating,
                comment
            });

            document.getElementById('reviewModal').classList.remove('active');
            showToast('Thank you! Your review has been published.', 'success');
            await this.loadNearbyEstablishments();
        } catch (e) {
            showToast('Review failed: ' + e.message, 'error');
        }
    }
};

window.customerApp = customerApp;
window.selectEstablishmentFromMap = (id) => customerApp.openEstablishment(id);

document.addEventListener('DOMContentLoaded', () => {
    customerApp.init();
});
