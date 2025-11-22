const express = require('express');
const cors = require('cors');
const { mongoose } = require('./db');

const app = express();
app.use(express.json());
app.use(cors());

// Import routes
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const productRoutes = require('./routes/products');
const orderRoutes = require('./routes/orders');
const cartRoutes = require('./routes/cart');
const promotionRoutes = require('./routes/promotions');
const reportRoutes = require('./routes/reports');
const inventoryRoutes = require('./routes/inventory');
const statisticsRoutes = require('./routes/statistics');

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/products', productRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/cart', cartRoutes);
app.use('/api/promotions', promotionRoutes);
app.use('/api/reports', reportRoutes);
app.use('/api/inventory', inventoryRoutes);
app.use('/api/statistics', statisticsRoutes);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`API server listening on port ${PORT}`);
});
