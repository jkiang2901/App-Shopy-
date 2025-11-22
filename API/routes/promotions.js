const express = require('express');
const router = express.Router();
const Promotion = require('../models/promotion');
const { authenticate, isAdminOrSeller } = require('../middleware/auth');

// Get all promotions (public for customers, all for admin/seller)
router.get('/', authenticate, async (req, res) => {
  try {
    let promotions;
    
    if (req.user.role === 'customer') {
      promotions = await Promotion.find({ 
        isActive: true,
        startDate: { $lte: new Date() },
        endDate: { $gte: new Date() }
      });
    } else if (req.user.role === 'seller') {
      promotions = await Promotion.find({ 
        $or: [
          { sellerId: req.user._id },
          { sellerId: null } // Admin promotions
        ]
      }).sort({ createdAt: -1 });
    } else if (req.user.role === 'admin') {
      promotions = await Promotion.find().sort({ createdAt: -1 });
    }

    res.json(promotions);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get promotion by code
router.get('/code/:code', authenticate, async (req, res) => {
  try {
    const promotion = await Promotion.findOne({ code: req.params.code });
    
    if (!promotion) {
      return res.status(404).json({ error: 'Promotion not found' });
    }

    if (!promotion.isActive) {
      return res.status(400).json({ error: 'Promotion is not active' });
    }

    const now = new Date();
    if (now < promotion.startDate || now > promotion.endDate) {
      return res.status(400).json({ error: 'Promotion is not valid at this time' });
    }

    res.json(promotion);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Create promotion (Admin or Seller)
router.post('/', authenticate, isAdminOrSeller, async (req, res) => {
  try {
    const promotionData = {
      ...req.body,
      sellerId: req.user.role === 'admin' ? null : req.user._id
    };

    const promotion = new Promotion(promotionData);
    await promotion.save();

    res.status(201).json(promotion);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Update promotion
router.put('/:id', authenticate, isAdminOrSeller, async (req, res) => {
  try {
    const promotion = await Promotion.findById(req.params.id);
    
    if (!promotion) {
      return res.status(404).json({ error: 'Promotion not found' });
    }

    // Seller can only update their own promotions
    if (req.user.role === 'seller' && promotion.sellerId?.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }

    Object.assign(promotion, req.body);
    await promotion.save();

    res.json(promotion);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Delete promotion
router.delete('/:id', authenticate, isAdminOrSeller, async (req, res) => {
  try {
    const promotion = await Promotion.findById(req.params.id);
    
    if (!promotion) {
      return res.status(404).json({ error: 'Promotion not found' });
    }

    // Seller can only delete their own promotions
    if (req.user.role === 'seller' && promotion.sellerId?.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }

    await Promotion.findByIdAndDelete(req.params.id);
    res.json({ message: 'Promotion deleted' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;

