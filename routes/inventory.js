const express = require('express');
const router = express.Router();
const Inventory = require('../models/inventory');
const Product = require('../models/product');
const { authenticate, isSeller } = require('../middleware/auth');

// Get inventory history (Seller)
router.get('/', authenticate, isSeller, async (req, res) => {
  try {
    const { productId } = req.query;
    const query = { sellerId: req.user._id };
    
    if (productId) {
      query.productId = productId;
    }

    const inventory = await Inventory.find(query)
      .populate('productId')
      .sort({ createdAt: -1 });

    res.json(inventory);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Import/Export/Adjust inventory
router.post('/', authenticate, isSeller, async (req, res) => {
  try {
    const { productId, quantity, type, note } = req.body;

    if (!productId || quantity === undefined || !type) {
      return res.status(400).json({ error: 'Product ID, quantity, and type are required' });
    }

    // Query product directly from database to get raw sellerId value
    const productDoc = await Product.findById(productId);
    if (!productDoc) {
      return res.status(404).json({ error: 'Product not found' });
    }

    // Check if product belongs to seller or is unassigned (null/undefined sellerId)
    // If product has no sellerId, assign it to current seller
    const currentSellerId = req.user._id.toString();
    
    // Get raw sellerId value - use toObject() to get plain object
    const productObj = productDoc.toObject();
    const rawSellerId = productObj.sellerId;
    
    console.log('=== INVENTORY DEBUG ===');
    console.log('Product ID:', productId);
    console.log('Product sellerId (from toObject):', rawSellerId);
    console.log('Product sellerId type:', typeof rawSellerId);
    console.log('Product sellerId === null:', rawSellerId === null);
    console.log('Product sellerId == null:', rawSellerId == null);
    console.log('Current user ID:', currentSellerId);
    
    // Check if sellerId is null or undefined
    if (rawSellerId === null || rawSellerId === undefined) {
      // Product has no sellerId, assign it to current seller
      console.log('Product has no sellerId (null/undefined) - assigning to current seller:', currentSellerId);
      productDoc.sellerId = req.user._id;
    } else {
      // Product has a sellerId, check if it matches current seller
      // Handle both ObjectId and string
      let productSellerId;
      if (rawSellerId.toString) {
        productSellerId = rawSellerId.toString();
      } else if (rawSellerId._id) {
        productSellerId = rawSellerId._id.toString();
      } else {
        productSellerId = String(rawSellerId);
      }
      console.log('Product sellerId (string):', productSellerId);
      
      if (productSellerId !== currentSellerId) {
        console.log('Access denied: Product belongs to different seller');
        console.log('Expected:', currentSellerId, 'Got:', productSellerId);
        return res.status(403).json({ error: 'Access denied. Product already belongs to another seller.' });
      }
      console.log('Product belongs to current seller - OK');
    }
    console.log('=== END DEBUG ===');

    // Update product quantity
    if (type === 'import') {
      productDoc.quantity += quantity;
    } else if (type === 'export') {
      if (productDoc.quantity < quantity) {
        return res.status(400).json({ error: 'Insufficient stock' });
      }
      productDoc.quantity -= quantity;
    } else if (type === 'adjustment') {
      productDoc.quantity = quantity;
    }

    productDoc.updatedAt = Date.now();
    await productDoc.save();

    // Create inventory record
    const inventory = new Inventory({
      sellerId: req.user._id,
      productId,
      quantity: type === 'adjustment' ? quantity : Math.abs(quantity),
      type,
      note
    });

    await inventory.save();
    await inventory.populate('productId');

    res.status(201).json(inventory);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

module.exports = router;

