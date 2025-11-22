const express = require('express');
const router = express.Router();
const Report = require('../models/report');
const { authenticate, isAdmin } = require('../middleware/auth');

// Create report (Customer or Seller)
router.post('/', authenticate, async (req, res) => {
  try {
    const { reportedUserId, reportType, reason, description } = req.body;

    if (!reportedUserId || !reportType || !reason) {
      return res.status(400).json({ error: 'Reported user ID, report type, and reason are required' });
    }

    if (reportType === 'seller' && req.user.role !== 'customer') {
      return res.status(403).json({ error: 'Only customers can report sellers' });
    }

    if (reportType === 'buyer' && req.user.role !== 'seller') {
      return res.status(403).json({ error: 'Only sellers can report buyers' });
    }

    const report = new Report({
      reporterId: req.user._id,
      reportedUserId,
      reportType,
      reason,
      description
    });

    await report.save();
    res.status(201).json(report);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Get reports (Admin only)
router.get('/', authenticate, isAdmin, async (req, res) => {
  try {
    const { status } = req.query;
    const query = status ? { status } : {};
    
    const reports = await Report.find(query)
      .populate('reporterId', 'name email role')
      .populate('reportedUserId', 'name email role')
      .sort({ createdAt: -1 });

    res.json(reports);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get report by ID (Admin only)
router.get('/:id', authenticate, isAdmin, async (req, res) => {
  try {
    const report = await Report.findById(req.params.id)
      .populate('reporterId', 'name email role')
      .populate('reportedUserId', 'name email role');

    if (!report) {
      return res.status(404).json({ error: 'Report not found' });
    }

    res.json(report);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Update report status (Admin only)
router.put('/:id/status', authenticate, isAdmin, async (req, res) => {
  try {
    const { status, adminNotes } = req.body;

    const report = await Report.findById(req.params.id);
    if (!report) {
      return res.status(404).json({ error: 'Report not found' });
    }

    report.status = status;
    if (adminNotes) {
      report.adminNotes = adminNotes;
    }
    report.updatedAt = Date.now();
    await report.save();

    res.json(report);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

module.exports = router;

