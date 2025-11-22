// API Base URL - Change this to match your server
// For local development: http://localhost:3000
// For production: your production API URL
const API_BASE_URL = 'http://localhost:3000';

// Check if user is authenticated
function checkAuth() {
    const token = localStorage.getItem('adminToken');
    const user = JSON.parse(localStorage.getItem('adminUser') || '{}');
    
    // If on login page, don't redirect
    if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/') {
        if (token && user.role === 'admin') {
            window.location.href = 'dashboard.html';
        }
        return;
    }
    
    // If not on login page and not authenticated, redirect to login
    if (!token || user.role !== 'admin') {
        window.location.href = 'index.html';
    }
}

// Logout function
function logout() {
    if (confirm('Bạn có chắc muốn đăng xuất?')) {
        localStorage.removeItem('adminToken');
        localStorage.removeItem('adminUser');
        window.location.href = 'index.html';
    }
}

// Format date
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}

// Show error message
function showError(elementId, message) {
    const errorDiv = document.getElementById(elementId);
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        // Auto hide after 5 seconds
        setTimeout(() => {
            hideError(elementId);
        }, 5000);
    }
}

// Hide error message
function hideError(elementId) {
    const errorDiv = document.getElementById(elementId);
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
}

// Show success message
function showSuccess(message, container = null) {
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.textContent = '✅ ' + message;
    successDiv.style.display = 'block';
    
    if (container) {
        container.insertBefore(successDiv, container.firstChild);
    } else {
        document.body.insertBefore(successDiv, document.body.firstChild);
    }
    
    // Auto hide after 3 seconds
    setTimeout(() => {
        successDiv.style.animation = 'slideInRight 0.5s ease-out reverse';
        setTimeout(() => {
            successDiv.remove();
        }, 500);
    }, 3000);
}

// Show modal with animation
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
        // Force reflow
        modal.offsetHeight;
        modal.classList.add('show');
        // Prevent body scroll
        document.body.style.overflow = 'hidden';
        
        // Close on outside click
        modal.addEventListener('click', function closeOnOutside(e) {
            if (e.target === modal) {
                hideModal(modalId);
                modal.removeEventListener('click', closeOnOutside);
            }
        });
    }
}

// Hide modal with animation
function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }, 300);
    }
}

// Get auth headers
function getAuthHeaders() {
    const token = localStorage.getItem('adminToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

// Make authenticated API call
async function apiCall(url, options = {}) {
    const token = localStorage.getItem('adminToken');
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };
    
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...(options.headers || {})
        }
    };
    
    const response = await fetch(`${API_BASE_URL}${url}`, mergedOptions);
    
    if (response.status === 401) {
        logout();
        throw new Error('Unauthorized');
    }
    
    return response;
}

