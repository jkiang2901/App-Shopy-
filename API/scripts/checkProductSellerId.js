const { mongoose } = require('../db');
const Product = require('../models/product');

async function checkProductSellerId() {
  try {
    // Wait for connection
    if (mongoose.connection.readyState === 0) {
      await new Promise((resolve) => {
        mongoose.connection.once('connected', resolve);
      });
    }
    
    const productId = '691f3aab0366e596e8f6c660';
    const product = await Product.findById(productId);
    
    if (!product) {
      console.log('Product not found');
      return;
    }
    
    console.log('=== PRODUCT SELLER ID CHECK ===');
    console.log('Product ID:', productId);
    console.log('Product name:', product.name);
    console.log('Product sellerId (raw):', product.sellerId);
    console.log('Product sellerId type:', typeof product.sellerId);
    console.log('Product sellerId === null:', product.sellerId === null);
    console.log('Product sellerId == null:', product.sellerId == null);
    
    // Check in database directly
    const productObj = product.toObject();
    console.log('Product sellerId (from toObject):', productObj.sellerId);
    console.log('Product sellerId (from toObject) === null:', productObj.sellerId === null);
    
    // Check all products with null sellerId
    const productsWithNullSellerId = await Product.find({ sellerId: null });
    console.log('\nProducts with null sellerId:', productsWithNullSellerId.length);
    
    // Check all products
    const allProducts = await Product.find({});
    console.log('Total products:', allProducts.length);
    for (let p of allProducts) {
      const pObj = p.toObject();
      console.log(`Product ${p._id}: sellerId = ${pObj.sellerId} (type: ${typeof pObj.sellerId})`);
    }
    
    await mongoose.disconnect();
  } catch (err) {
    console.error('Error:', err);
    process.exit(1);
  }
}

checkProductSellerId();

