const { mongoose } = require('../db');

const orderSchema = new mongoose.Schema({
  customerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  sellerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: false }, // Allow null for unassigned products
  items: [{
    productId: { type: mongoose.Schema.Types.ObjectId, ref: 'Product', required: true },
    quantity: { type: Number, required: true },
    price: { type: Number, required: true }
  }],
  totalAmount: { type: Number, required: true },
  discountAmount: { type: Number, default: 0 },
  finalAmount: { type: Number, required: true },
  paymentMethod: { type: String, required: true },
  deliveryAddress: { type: String, required: true },
  status: { 
    type: String, 
    enum: ['pending', 'confirmed', 'shipping', 'delivered', 'cancelled'],
    default: 'pending'
  },
  promotionId: { type: mongoose.Schema.Types.ObjectId, ref: 'Promotion' },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
}, { collection: 'orders' });

const Order = mongoose.model('Order', orderSchema);
module.exports = Order;

