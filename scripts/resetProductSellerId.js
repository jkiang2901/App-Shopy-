const { mongoose } = require('../db');
const Product = require('../models/product');

async function resetProductSellerId() {
  try {
    // Wait for connection
    if (mongoose.connection.readyState === 0) {
      await new Promise((resolve) => {
        mongoose.connection.once('connected', resolve);
      });
    }
    
    console.log('Resetting all product sellerId to null...');
    
    const result = await Product.updateMany(
      {},
      { $set: { sellerId: null } }
    );
    
    console.log(`Updated ${result.modifiedCount} products`);
    
    // Verify
    const productsWithNullSellerId = await Product.find({ sellerId: null });
    console.log(`Products with null sellerId: ${productsWithNullSellerId.length}`);
    
    await mongoose.disconnect();
    console.log('Done!');
  } catch (err) {
    console.error('Error:', err);
    process.exit(1);
  }
}

resetProductSellerId();

