const { mongoose } = require('../db');

const inventorySchema = new mongoose.Schema({
  sellerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  productId: { type: mongoose.Schema.Types.ObjectId, ref: 'Product', required: true },
  quantity: { type: Number, required: true, min: 0 },
  type: { type: String, enum: ['import', 'export', 'adjustment'], required: true },
  note: { type: String },
  createdAt: { type: Date, default: Date.now }
}, { collection: 'inventory' });

const Inventory = mongoose.model('Inventory', inventorySchema);
module.exports = Inventory;

