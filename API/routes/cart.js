const express = require('express');
const router = express.Router();
const Cart = require('../models/cart');
const Product = require('../models/product');
const { authenticate, isCustomer } = require('../middleware/auth');

// Get cart (Customer)
router.get('/', authenticate, isCustomer, async (req, res) => {
  try {
    let cart = await Cart.findOne({ customerId: req.user._id }).populate('items.productId');
    
    if (!cart) {
      cart = new Cart({ customerId: req.user._id, items: [] });
      await cart.save();
    }

    res.json(cart);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Add item to cart
router.post('/items', authenticate, isCustomer, async (req, res) => {
  try {
    const { productId, quantity } = req.body;

    if (!productId || !quantity || quantity < 1) {
      return res.status(400).json({ error: 'Product ID and quantity (>=1) are required' });
    }

    const product = await Product.findById(productId);
    if (!product) {
      return res.status(404).json({ error: 'Product not found' });
    }

    if (product.quantity < quantity) {
      return res.status(400).json({ error: 'Insufficient stock' });
    }

    let cart = await Cart.findOne({ customerId: req.user._id });
    
    if (!cart) {
      cart = new Cart({ customerId: req.user._id, items: [] });
    }

    // Check if item already exists
    const existingItemIndex = cart.items.findIndex(
      item => item.productId.toString() === productId
    );

    if (existingItemIndex >= 0) {
      cart.items[existingItemIndex].quantity += quantity;
    } else {
      cart.items.push({ productId, quantity });
    }

    cart.updatedAt = Date.now();
    await cart.save();

    await cart.populate('items.productId');
    res.json(cart);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Update item quantity
router.put('/items/:productId', authenticate, isCustomer, async (req, res) => {
  try {
    const { quantity } = req.body;

    if (!quantity || quantity < 1) {
      return res.status(400).json({ error: 'Quantity must be at least 1' });
    }

    const cart = await Cart.findOne({ customerId: req.user._id });
    if (!cart) {
      return res.status(404).json({ error: 'Cart not found' });
    }

    const itemIndex = cart.items.findIndex(
      item => item.productId.toString() === req.params.productId
    );

    if (itemIndex === -1) {
      return res.status(404).json({ error: 'Item not found in cart' });
    }

    // Check stock
    const product = await Product.findById(req.params.productId);
    if (!product || product.quantity < quantity) {
      return res.status(400).json({ error: 'Insufficient stock' });
    }

    cart.items[itemIndex].quantity = quantity;
    cart.updatedAt = Date.now();
    await cart.save();

    await cart.populate('items.productId');
    res.json(cart);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Remove item from cart
router.delete('/items/:productId', authenticate, isCustomer, async (req, res) => {
  try {
    const cart = await Cart.findOne({ customerId: req.user._id });
    if (!cart) {
      return res.status(404).json({ error: 'Cart not found' });
    }

    cart.items = cart.items.filter(
      item => item.productId.toString() !== req.params.productId
    );
    cart.updatedAt = Date.now();
    await cart.save();

    await cart.populate('items.productId');
    res.json(cart);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Clear cart
router.delete('/', authenticate, isCustomer, async (req, res) => {
  try {
    const cart = await Cart.findOne({ customerId: req.user._id });
    if (!cart) {
      return res.status(404).json({ error: 'Cart not found' });
    }

    cart.items = [];
    cart.updatedAt = Date.now();
    await cart.save();

    res.json({ message: 'Cart cleared' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;

