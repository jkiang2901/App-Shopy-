const express = require('express');
const router = express.Router();
const User = require('../models/user');
const { authenticate, isAdmin, isAdminOrSeller } = require('../middleware/auth');

// Get current user profile
router.get('/me', authenticate, async (req, res) => {
  try {
    const user = await User.findById(req.user._id).select('-password');
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Update current user profile
router.put('/me', authenticate, async (req, res) => {
  try {
    const { name, phone, address } = req.body;
    const user = await User.findById(req.user._id);
    
    if (name) user.name = name;
    if (phone) user.phone = phone;
    if (address) user.address = address;
    
    user.updatedAt = Date.now();
    await user.save();
    
    const userResponse = user.toObject();
    delete userResponse.password;
    res.json(userResponse);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Get all users (Admin only - for managing customers and sellers)
router.get('/', authenticate, isAdmin, async (req, res) => {
  try {
    const { role, search } = req.query;
    const query = {};
    
    if (role) {
      query.role = role;
    }
    
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } }
      ];
    }

    const users = await User.find(query).select('-password').exec();
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get customers (Seller - manage their customers)
router.get('/customers', authenticate, isAdminOrSeller, async (req, res) => {
  try {
    const { search } = req.query;
    const query = { role: 'customer' };
    
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } }
      ];
    }

    // Seller can only see customers who have ordered from them
    if (req.user.role === 'seller') {
      const Order = require('../models/order');
      const orders = await Order.find({ sellerId: req.user._id }).distinct('customerId');
      query._id = { $in: orders };
    }

    const customers = await User.find(query).select('-password').exec();
    res.json(customers);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Create user (Admin only - for creating sellers)
router.post('/', authenticate, isAdmin, async (req, res) => {
  try {
    const userData = req.body;
    // Admin can create sellers or customers
    if (userData.role === 'admin') {
      return res.status(403).json({ error: 'Cannot create admin users' });
    }
    
    const user = new User(userData);
    await user.save();
    
    const userResponse = user.toObject();
    delete userResponse.password;
    res.status(201).json(userResponse);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Read user by id
router.get('/:id', authenticate, async (req, res) => {
  try {
    const user = await User.findById(req.params.id).select('-password').exec();
    if (!user) return res.status(404).json({ error: 'User not found' });
    
    // Check access
    if (req.user.role === 'customer' && user._id.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }
    
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Update user (Admin or own profile)
router.put('/:id', authenticate, async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user) return res.status(404).json({ error: 'User not found' });
    
    // Check access
    if (req.user.role !== 'admin' && user._id.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }
    
    // Only admin can change role
    if (req.body.role && req.user.role !== 'admin') {
      delete req.body.role;
    }
    
    Object.assign(user, req.body);
    user.updatedAt = Date.now();
    await user.save();
    
    const userResponse = user.toObject();
    delete userResponse.password;
    res.json(userResponse);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Delete user (Admin only)
router.delete('/:id', authenticate, isAdmin, async (req, res) => {
  try {
    const user = await User.findByIdAndDelete(req.params.id).exec();
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({ message: 'User deleted' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
