const { mongoose } = require('../db');

const reportSchema = new mongoose.Schema({
  reporterId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  reportedUserId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  reportType: { type: String, enum: ['seller', 'buyer'], required: true },
  reason: { type: String, required: true },
  description: { type: String },
  status: { 
    type: String, 
    enum: ['pending', 'reviewing', 'resolved', 'dismissed'],
    default: 'pending'
  },
  adminNotes: { type: String },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
}, { collection: 'reports' });

const Report = mongoose.model('Report', reportSchema);
module.exports = Report;

