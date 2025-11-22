const { mongoose } = require('../db');

const promotionSchema = new mongoose.Schema({
  code: { type: String, required: true, unique: true },
  name: { type: String, required: true },
  description: { type: String },
  discountType: { type: String, enum: ['percentage', 'fixed'], required: true },
  discountValue: { type: Number, required: true },
  minPurchaseAmount: { type: Number, default: 0 },
  maxDiscountAmount: { type: Number },
  startDate: { type: Date, required: true },
  endDate: { type: Date, required: true },
  sellerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' }, // null for admin promotions
  isActive: { type: Boolean, default: true },
  usageLimit: { type: Number },
  usedCount: { type: Number, default: 0 },
  createdAt: { type: Date, default: Date.now }
}, { collection: 'promotions' });

const Promotion = mongoose.model('Promotion', promotionSchema);
module.exports = Promotion;

