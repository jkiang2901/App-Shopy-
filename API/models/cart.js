const { mongoose } = require('../db');

const cartSchema = new mongoose.Schema({
  customerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, unique: true },
  items: [{
    productId: { type: mongoose.Schema.Types.ObjectId, ref: 'Product', required: true },
    quantity: { type: Number, required: true, min: 1 }
  }],
  updatedAt: { type: Date, default: Date.now }
}, { collection: 'carts' });

const Cart = mongoose.model('Cart', cartSchema);
module.exports = Cart;

