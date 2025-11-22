const { mongoose } = require('../db');
const User = require('../models/user');

async function createDefaultAdmin() {
  try {
    // Wait for MongoDB connection
    await new Promise((resolve) => {
      if (mongoose.connection.readyState === 1) {
        resolve();
      } else {
        mongoose.connection.once('open', resolve);
      }
    });

    console.log('Connected to MongoDB');

    // Default admin credentials
    const defaultAdmin = {
      name: 'Administrator',
      email: 'admin@example.com',
      password: 'admin123', // Will be hashed by pre-save hook
      role: 'admin',
      isActive: true,
      phone: '',
      address: ''
    };

    // Check if admin user already exists
    const existingAdmin = await User.findOne({ email: defaultAdmin.email });
    
    if (existingAdmin) {
      console.log('Admin user already exists!');
      console.log('Email:', existingAdmin.email);
      console.log('Name:', existingAdmin.name);
      console.log('Role:', existingAdmin.role);
      console.log('\nTo reset password, you can use:');
      console.log('  Email: ' + defaultAdmin.email);
      console.log('  Password: ' + defaultAdmin.password);
      console.log('\nOr run: node scripts/fixAdminPassword.js');
    } else {
      console.log('Creating default admin user...');
      
      // Create new admin user (password will be hashed by pre-save hook)
      const admin = new User(defaultAdmin);
      await admin.save();
      
      console.log('\n=== Default Admin Created Successfully ===');
      console.log('Email:', defaultAdmin.email);
      console.log('Password:', defaultAdmin.password);
      console.log('Name:', defaultAdmin.name);
      console.log('Role:', defaultAdmin.role);
      console.log('\nYou can now login with these credentials!');
    }

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('Error:', error);
    await mongoose.connection.close();
    process.exit(1);
  }
}

createDefaultAdmin();

