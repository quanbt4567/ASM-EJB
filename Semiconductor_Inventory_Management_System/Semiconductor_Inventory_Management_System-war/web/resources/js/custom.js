/**
 * SEMICONDUCTOR INVENTORY MANAGEMENT SYSTEM
 * Custom JavaScript Functions
 * Author: MINH_QUAN
 * Created: 2025-09-16
 */

// ========== GLOBAL CONFIGURATION ==========
const SIMSApp = {
    version: '1.0.0',
    apiVersion: 'v1',
    loadingOverlay: null,
    
    // Configuration
    config: {
        tablePageSize: 25,
        refreshInterval: 300000, // 5 minutes
        animationDuration: 300,
        debounceDelay: 500,
        chartColors: {
            primary: '#007bff',
            success: '#28a745',
            warning: '#ffc107',
            danger: '#dc3545',
            info: '#17a2b8',
            secondary: '#6c757d'
        }
    },
    
    // Utility functions
    utils: {},
    
    // Chart instances
    charts: {},
    
    // DataTable instances
    tables: {},
    
    // Form validators
    validators: {}
};

// ========== UTILITY FUNCTIONS ==========
SIMSApp.utils = {
    /**
     * Format number with thousands separator
     */
    formatNumber: function(num) {
        if (num === null || num === undefined) return '0';
        return parseInt(num).toLocaleString();
    },
    
    /**
     * Format currency
     */
    formatCurrency: function(amount) {
        if (amount === null || amount === undefined) return '$0.00';
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    },
    
    /**
     * Format date
     */
    formatDate: function(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    },
    
    /**
     * Format date and time
     */
    formatDateTime: function(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },
    
    /**
     * Debounce function
     */
    debounce: function(func, delay) {
        let timeoutId;
        return function (...args) {
            clearTimeout(timeoutId);
            timeoutId = setTimeout(() => func.apply(this, args), delay);
        };
    },
    
    /**
     * Show toast notification
     */
    showToast: function(message, type = 'info', duration = 3000) {
        const toastContainer = document.getElementById('toast-container') || this.createToastContainer();
        const toast = this.createToast(message, type, duration);
        toastContainer.appendChild(toast);
        
        // Show toast
        setTimeout(() => toast.classList.add('show'), 100);
        
        // Auto hide
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, duration);
    },
    
    /**
     * Create toast container if it doesn't exist
     */
    createToastContainer: function() {
        const container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'position-fixed top-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
        return container;
    },
    
    /**
     * Create toast element
     */
    createToast: function(message, type, duration) {
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type} border-0`;
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" onclick="this.parentElement.parentElement.remove()"></button>
            </div>
        `;
        return toast;
    },
    
    /**
     * Show confirmation dialog
     */
    confirm: function(message, callback) {
        if (confirm(message)) {
            callback();
        }
    },
    
    /**
     * Generate random ID
     */
    generateId: function() {
        return 'id_' + Math.random().toString(36).substr(2, 9);
    },
    
    /**
     * Validate email format
     */
    isValidEmail: function(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    },
    
    /**
     * Validate phone number
     */
    isValidPhone: function(phone) {
        const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
        return phoneRegex.test(phone.replace(/[\s\-\(\)]/g, ''));
    }
};

// ========== LOADING OVERLAY ==========
SIMSApp.loading = {
    show: function(message = 'Loading...') {
        if (SIMSApp.loadingOverlay) {
            this.hide();
        }
        
        SIMSApp.loadingOverlay = document.createElement('div');
        SIMSApp.loadingOverlay.className = 'loading-overlay';
        SIMSApp.loadingOverlay.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
                <div class="loading-text mt-3">${message}</div>
            </div>
        `;
        document.body.appendChild(SIMSApp.loadingOverlay);
    },
    
    hide: function() {
        if (SIMSApp.loadingOverlay) {
            SIMSApp.loadingOverlay.remove();
            SIMSApp.loadingOverlay = null;
        }
    }
};

// ========== FORM VALIDATION ==========
SIMSApp.validators = {
    /**
     * Initialize form validation
     */
    init: function() {
        const forms = document.querySelectorAll('.needs-validation');
        forms.forEach(form => {
            form.addEventListener('submit', this.handleFormSubmit.bind(this));
        });
    },
    
    /**
     * Handle form submission
     */
    handleFormSubmit: function(event) {
        const form = event.target;
        if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
            this.showValidationErrors(form);
        }
        form.classList.add('was-validated');
    },
    
    /**
     * Show validation errors
     */
    showValidationErrors: function(form) {
        const invalidFields = form.querySelectorAll(':invalid');
        if (invalidFields.length > 0) {
            invalidFields[0].focus();
            SIMSApp.utils.showToast('Please correct the highlighted fields', 'danger');
        }
    },
    
    /**
     * Real-time validation
     */
    setupRealTimeValidation: function() {
        const inputs = document.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.addEventListener('blur', this.validateField.bind(this));
            input.addEventListener('input', this.clearFieldErrors.bind(this));
        });
    },
    
    /**
     * Validate individual field
     */
    validateField: function(event) {
        const field = event.target;
        const isValid = field.checkValidity();
        
        if (!isValid) {
            field.classList.add('is-invalid');
            field.classList.remove('is-valid');
        } else {
            field.classList.add('is-valid');
            field.classList.remove('is-invalid');
        }
    },
    
    /**
     * Clear field errors
     */
    clearFieldErrors: function(event) {
        const field = event.target;
        field.classList.remove('is-invalid', 'is-valid');
    }
};

// ========== DATA TABLES ==========
SIMSApp.dataTables = {
    /**
     * Initialize all data tables
     */
    init: function() {
        const tables = document.querySelectorAll('.data-table');
        tables.forEach(table => {
            this.initializeTable(table);
        });
    },
    
    /**
     * Initialize individual table
     */
    initializeTable: function(table) {
        const tableId = table.id || SIMSApp.utils.generateId();
        table.id = tableId;
        
        const options = {
            pageLength: SIMSApp.config.tablePageSize,
            responsive: true,
            lengthMenu: [[10, 25, 50, 100], [10, 25, 50, 100]],
            dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
                 '<"row"<"col-sm-12"tr>>' +
                 '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
            language: {
                search: '_INPUT_',
                searchPlaceholder: 'Search...',
                lengthMenu: 'Show _MENU_ entries',
                info: 'Showing _START_ to _END_ of _TOTAL_ entries',
                infoEmpty: 'No entries found',
                infoFiltered: '(filtered from _MAX_ total entries)',
                paginate: {
                    first: 'First',
                    last: 'Last',
                    next: 'Next',
                    previous: 'Previous'
                }
            },
            columnDefs: [
                {
                    targets: 'no-sort',
                    orderable: false
                }
            ]
        };
        
        // Add export buttons if requested
        if (table.classList.contains('exportable')) {
            options.dom = '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
                         '<"row"<"col-sm-12 col-md-6"B><"col-sm-12 col-md-6">>' +
                         '<"row"<"col-sm-12"tr>>' +
                         '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>';
            options.buttons = [
                'copy', 'csv', 'excel', 'pdf', 'print'
            ];
        }
        
        SIMSApp.tables[tableId] = $(table).DataTable(options);
    },
    
    /**
     * Refresh table data
     */
    refresh: function(tableId) {
        if (SIMSApp.tables[tableId]) {
            SIMSApp.tables[tableId].ajax.reload();
        }
    },
    
    /**
     * Get selected rows
     */
    getSelectedRows: function(tableId) {
        if (SIMSApp.tables[tableId]) {
            return SIMSApp.tables[tableId].rows('.selected').data().toArray();
        }
        return [];
    }
};

// ========== CHARTS ==========
SIMSApp.charts = {
    /**
     * Initialize dashboard charts
     */
    init: function() {
        this.initInventoryChart();
        this.initTransactionChart();
        this.initSupplierChart();
    },
    
    /**
     * Initialize inventory overview chart
     */
    initInventoryChart: function() {
        const ctx = document.getElementById('inventoryChart');
        if (!ctx) return;
        
        SIMSApp.charts.inventory = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['In Stock', 'Low Stock', 'Out of Stock'],
                datasets: [{
                    data: [0, 0, 0],
                    backgroundColor: [
                        SIMSApp.config.chartColors.success,
                        SIMSApp.config.chartColors.warning,
                        SIMSApp.config.chartColors.danger
                    ],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    },
    
    /**
     * Initialize transaction trend chart
     */
    initTransactionChart: function() {
        const ctx = document.getElementById('transactionChart');
        if (!ctx) return;
        
        SIMSApp.charts.transaction = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Transactions',
                    data: [],
                    borderColor: SIMSApp.config.chartColors.primary,
                    backgroundColor: SIMSApp.config.chartColors.primary + '20',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    },
    
    /**
     * Initialize supplier performance chart
     */
    initSupplierChart: function() {
        const ctx = document.getElementById('supplierChart');
        if (!ctx) return;
        
        SIMSApp.charts.supplier = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Order Count',
                    data: [],
                    backgroundColor: SIMSApp.config.chartColors.info,
                    borderColor: SIMSApp.config.chartColors.info,
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    },
    
    /**
     * Update chart data
     */
    updateChart: function(chartName, data) {
        const chart = SIMSApp.charts[chartName];
        if (chart) {
            chart.data = data;
            chart.update();
        }
    }
};

// ========== DASHBOARD FUNCTIONS ==========
SIMSApp.dashboard = {
    /**
     * Initialize dashboard
     */
    init: function() {
        this.loadMetrics();
        this.loadRecentActivity();
        this.startAutoRefresh();
    },
    
    /**
     * Load dashboard metrics
     */
    loadMetrics: function() {
        SIMSApp.loading.show('Loading dashboard metrics...');
        
        // Simulate API call - replace with actual API endpoint
        setTimeout(() => {
            this.updateMetrics({
                totalComponents: 1250,
                lowStock: 15,
                totalSuppliers: 45,
                pendingOrders: 8
            });
            SIMSApp.loading.hide();
        }, 1000);
    },
    
    /**
     * Update metric cards
     */
    updateMetrics: function(metrics) {
        const elements = {
            totalComponents: document.getElementById('totalComponents'),
            lowStock: document.getElementById('lowStock'),
            totalSuppliers: document.getElementById('totalSuppliers'),
            pendingOrders: document.getElementById('pendingOrders')
        };
        
        Object.keys(metrics).forEach(key => {
            if (elements[key]) {
                this.animateCounter(elements[key], metrics[key]);
            }
        });
    },
    
    /**
     * Animate counter numbers
     */
    animateCounter: function(element, target) {
        const start = parseInt(element.textContent) || 0;
        const increment = (target - start) / 20;
        let current = start;
        
        const timer = setInterval(() => {
            current += increment;
            if ((increment > 0 && current >= target) || (increment < 0 && current <= target)) {
                current = target;
                clearInterval(timer);
            }
            element.textContent = SIMSApp.utils.formatNumber(Math.floor(current));
        }, 50);
    },
    
    /**
     * Load recent activity
     */
    loadRecentActivity: function() {
        // Simulate activity data - replace with actual API call
        const activities = [
            {
                type: 'info',
                icon: 'fas fa-plus',
                message: 'New component added: Arduino Uno R3',
                time: new Date()
            },
            {
                type: 'warning',
                icon: 'fas fa-exclamation-triangle',
                message: 'Low stock alert: Resistor 10kΩ',
                time: new Date(Date.now() - 300000)
            },
            {
                type: 'success',
                icon: 'fas fa-check',
                message: 'Order completed: Supply order #12345',
                time: new Date(Date.now() - 600000)
            }
        ];
        
        this.renderActivity(activities);
    },
    
    /**
     * Render activity feed
     */
    renderActivity: function(activities) {
        const container = document.getElementById('activityFeed');
        if (!container) return;
        
        container.innerHTML = activities.map(activity => `
            <div class="activity-item fade-in">
                <div class="activity-icon bg-${activity.type}">
                    <i class="${activity.icon}"></i>
                </div>
                <div class="activity-content">
                    <p class="mb-1">${activity.message}</p>
                    <small class="text-muted">${SIMSApp.utils.formatDateTime(activity.time)}</small>
                </div>
            </div>
        `).join('');
    },
    
    /**
     * Start auto refresh
     */
    startAutoRefresh: function() {
        setInterval(() => {
            this.loadMetrics();
            this.loadRecentActivity();
        }, SIMSApp.config.refreshInterval);
    }
};

// ========== COMPONENT MANAGEMENT ==========
SIMSApp.components = {
    /**
     * Initialize component management
     */
    init: function() {
        this.setupEventListeners();
        this.loadComponents();
    },
    
    /**
     * Setup event listeners
     */
    setupEventListeners: function() {
        // Add component button
        const addBtn = document.getElementById('addComponentBtn');
        if (addBtn) {
            addBtn.addEventListener('click', this.showAddModal.bind(this));
        }
        
        // Search input
        const searchInput = document.getElementById('componentSearch');
        if (searchInput) {
            searchInput.addEventListener('input', 
                SIMSApp.utils.debounce(this.searchComponents.bind(this), SIMSApp.config.debounceDelay)
            );
        }
    },
    
    /**
     * Load components
     */
    loadComponents: function() {
        SIMSApp.loading.show('Loading components...');
        
        // Simulate API call
        setTimeout(() => {
            SIMSApp.loading.hide();
            SIMSApp.utils.showToast('Components loaded successfully', 'success');
        }, 1000);
    },
    
    /**
     * Search components
     */
    searchComponents: function(query) {
        console.log('Searching components:', query);
        // Implement search logic
    },
    
    /**
     * Show add component modal
     */
    showAddModal: function() {
        const modal = new bootstrap.Modal(document.getElementById('addComponentModal'));
        modal.show();
    },
    
    /**
     * Delete component with confirmation
     */
    deleteComponent: function(componentId, componentName) {
        SIMSApp.utils.confirm(
            `Are you sure you want to delete "${componentName}"?`,
            () => {
                this.performDelete(componentId);
            }
        );
    },
    
    /**
     * Perform component deletion
     */
    performDelete: function(componentId) {
        SIMSApp.loading.show('Deleting component...');
        
        // Simulate API call
        setTimeout(() => {
            SIMSApp.loading.hide();
            SIMSApp.utils.showToast('Component deleted successfully', 'success');
            this.loadComponents();
        }, 1000);
    }
};

// ========== RESPONSIVE NAVIGATION ==========
SIMSApp.navigation = {
    /**
     * Initialize navigation
     */
    init: function() {
        this.setupMobileMenu();
        this.highlightActiveMenu();
    },
    
    /**
     * Setup mobile menu
     */
    setupMobileMenu: function() {
        const navbarToggler = document.querySelector('.navbar-toggler');
        const navbarCollapse = document.querySelector('.navbar-collapse');
        
        if (navbarToggler && navbarCollapse) {
            navbarToggler.addEventListener('click', () => {
                navbarCollapse.classList.toggle('show');
            });
        }
    },
    
    /**
     * Highlight active menu item
     */
    highlightActiveMenu: function() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
        
        navLinks.forEach(link => {
            if (link.getAttribute('href') === currentPath) {
                link.classList.add('active');
            }
        });
    }
};

// ========== SESSION MANAGEMENT ==========
SIMSApp.session = {
    /**
     * Check session status
     */
    checkSession: function() {
        // Implement session check logic
        const sessionTimeout = 30 * 60 * 1000; // 30 minutes
        
        setInterval(() => {
            this.pingServer();
        }, sessionTimeout / 2);
    },
    
    /**
     * Ping server to keep session alive
     */
    pingServer: function() {
        // Implement session ping
        console.log('Pinging server to maintain session');
    },
    
    /**
     * Handle session timeout
     */
    handleTimeout: function() {
        SIMSApp.utils.showToast('Session expired. Please log in again.', 'warning');
        setTimeout(() => {
            window.location.href = 'login.xhtml';
        }, 3000);
    }
};

// ========== KEYBOARD SHORTCUTS ==========
SIMSApp.shortcuts = {
    /**
     * Initialize keyboard shortcuts
     */
    init: function() {
        document.addEventListener('keydown', this.handleKeyDown.bind(this));
    },
    
    /**
     * Handle key combinations
     */
    handleKeyDown: function(event) {
        // Ctrl+S: Save form
        if (event.ctrlKey && event.key === 's') {
            event.preventDefault();
            this.saveCurrentForm();
        }
        
        // Ctrl+F: Focus search
        if (event.ctrlKey && event.key === 'f') {
            event.preventDefault();
            this.focusSearch();
        }
        
        // Escape: Close modals
        if (event.key === 'Escape') {
            this.closeModals();
        }
    },
    
    /**
     * Save current form
     */
    saveCurrentForm: function() {
        const form = document.querySelector('form:not([hidden])');
        if (form) {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.click();
            }
        }
    },
    
    /**
     * Focus search input
     */
    focusSearch: function() {
        const searchInput = document.querySelector('input[type="search"], input[placeholder*="search" i]');
        if (searchInput) {
            searchInput.focus();
            searchInput.select();
        }
    },
    
    /**
     * Close all open modals
     */
    closeModals: function() {
        const modals = document.querySelectorAll('.modal.show');
        modals.forEach(modal => {
            const modalInstance = bootstrap.Modal.getInstance(modal);
            if (modalInstance) {
                modalInstance.hide();
            }
        });
    }
};

// ========== INITIALIZATION ==========
document.addEventListener('DOMContentLoaded', function() {
    console.log('SIMS Application initializing...');
    
    // Initialize core modules
    SIMSApp.validators.init();
    SIMSApp.validators.setupRealTimeValidation();
    SIMSApp.navigation.init();
    SIMSApp.shortcuts.init();
    
    // Initialize page-specific modules
    if (document.getElementById('dashboardContent')) {
        SIMSApp.dashboard.init();
        SIMSApp.charts.init();
    }
    
    if (document.querySelector('.data-table')) {
        SIMSApp.dataTables.init();
    }
    
    if (document.getElementById('componentsPage')) {
        SIMSApp.components.init();
    }
    
    // Start session management
    SIMSApp.session.checkSession();
    
    console.log('SIMS Application initialized successfully');
});

// ========== GLOBAL ERROR HANDLING ==========
window.addEventListener('error', function(event) {
    console.error('Global error:', event.error);
    SIMSApp.utils.showToast('An error occurred. Please try again.', 'danger');
});

window.addEventListener('unhandledrejection', function(event) {
    console.error('Unhandled promise rejection:', event.reason);
    SIMSApp.utils.showToast('An error occurred. Please try again.', 'danger');
});

// Export for use in other scripts
window.SIMSApp = SIMSApp;