const express = require('express');
const router = express.Router();
const Product = require('../models/product');
const { authenticate, isAdminOrSeller } = require('../middleware/auth');

// Get all products (with filters for customers)
router.get('/', async (req, res) => {
  try {
    const { category, brand, minPrice, maxPrice, search, sellerId } = req.query;
    const query = { inStock: true };

    if (category) query.category = category;
    if (brand) query.brand = brand;
    if (sellerId) query.sellerId = sellerId;
    
    if (minPrice || maxPrice) {
      query.price = {};
      if (minPrice) query.price.$gte = parseFloat(minPrice);
      if (maxPrice) query.price.$lte = parseFloat(maxPrice);
    }

    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { description: { $regex: search, $options: 'i' } }
      ];
    }

    // Don't populate sellerId - return as string ID only
    const products = await Product.find(query)
      .sort({ createdAt: -1 })
      .exec();
    
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get product by id
router.get('/:id', async (req, res) => {
  try {
    // Don't populate sellerId - return as string ID only
    const product = await Product.findById(req.params.id).exec();
    
    if (!product) return res.status(404).json({ error: 'Product not found' });
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Create product (Seller or Admin)
router.post('/', authenticate, isAdminOrSeller, async (req, res) => {
  try {
    const productData = {
      ...req.body,
      sellerId: req.user.role === 'admin' ? (req.body.sellerId || req.user._id) : req.user._id
    };

    const product = new Product(productData);
    await product.save();
    // Don't populate sellerId - return as string ID only
    
    res.status(201).json(product);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Update product (Seller - own products, Admin - all)
router.put('/:id', authenticate, isAdminOrSeller, async (req, res) => {
  try {
    const product = await Product.findById(req.params.id);
    if (!product) return res.status(404).json({ error: 'Product not found' });

    // Seller can only update their own products
    if (req.user.role === 'seller' && product.sellerId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }

    Object.assign(product, req.body);
    product.updatedAt = Date.now();
    await product.save();
    // Don't populate sellerId - return as string ID only
    
    res.json(product);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Delete product (Seller - own products, Admin - all)
router.delete('/:id', authenticate, isAdminOrSeller, async (req, res) => {
  try {
    const product = await Product.findById(req.params.id);
    if (!product) return res.status(404).json({ error: 'Product not found' });

    // Seller can only delete their own products
    if (req.user.role === 'seller' && product.sellerId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }

    await Product.findByIdAndDelete(req.params.id);
    res.json({ message: 'Product deleted' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
