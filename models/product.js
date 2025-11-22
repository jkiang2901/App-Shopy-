const { mongoose } = require('../db');

const productSchema = new mongoose.Schema({
  name: { type: String, required: true },
  price: { type: Number, required: true },
  description: { type: String },
  images: [{ type: String }],
  category: { type: String },
  brand: { type: String },
  color: { type: String },
  size: { type: String },
  quantity: { type: Number, default: 0 },
  inStock: { type: Boolean, default: true },
  sellerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' }, // null for unassigned products
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
}, { collection: 'products' });

const Product = mongoose.model('Product', productSchema);
module.exports = Product;
