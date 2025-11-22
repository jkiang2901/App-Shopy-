const jwt = require('jsonwebtoken');
const User = require('../models/user');

const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key-change-in-production';

// Verify JWT token
const authenticate = async (req, res, next) => {
  try {
    const token = req.headers.authorization?.split(' ')[1];
    
    if (!token) {
      return res.status(401).json({ error: 'No token provided' });
    }

    const decoded = jwt.verify(token, JWT_SECRET);
    const user = await User.findById(decoded.userId).select('-password');
    
    if (!user || !user.isActive) {
      return res.status(401).json({ error: 'User not found or inactive' });
    }

    req.user = user;
    next();
  } catch (error) {
    res.status(401).json({ error: 'Invalid token' });
  }
};

// Check if user is admin
const isAdmin = (req, res, next) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ error: 'Admin access required' });
  }
  next();
};

// Check if user is seller
const isSeller = (req, res, next) => {
  if (req.user.role !== 'seller') {
    return res.status(403).json({ error: 'Seller access required' });
  }
  next();
};

// Check if user is customer
const isCustomer = (req, res, next) => {
  if (req.user.role !== 'customer') {
    return res.status(403).json({ error: 'Customer access required' });
  }
  next();
};

// Check if user is admin or seller
const isAdminOrSeller = (req, res, next) => {
  if (req.user.role !== 'admin' && req.user.role !== 'seller') {
    return res.status(403).json({ error: 'Admin or Seller access required' });
  }
  next();
};

module.exports = {
  authenticate,
  isAdmin,
  isSeller,
  isCustomer,
  isAdminOrSeller,
  JWT_SECRET
};

