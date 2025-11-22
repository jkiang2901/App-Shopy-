const { mongoose } = require('../db');
const User = require('../models/user');
const bcrypt = require('bcryptjs');

async function fixAdminPassword() {
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

    const email = 'nguyenvanan@example.com';
    const newPassword = 'abc123';

    // Find the admin user
    const user = await User.findOne({ email });
    
    if (!user) {
      console.log('User not found. Creating new admin user...');
      const hashedPassword = await bcrypt.hash(newPassword, 10);
      const newUser = new User({
        name: 'Nguyen Van An',
        email: email,
        password: hashedPassword,
        role: 'admin',
        isActive: true
      });
      // Skip the pre-save hook by using updateOne or setting password directly
      await User.collection.insertOne({
        name: newUser.name,
        email: newUser.email,
        password: hashedPassword,
        role: newUser.role,
        isActive: newUser.isActive,
        createdAt: new Date(),
        updatedAt: new Date()
      });
      console.log('Admin user created successfully!');
    } else {
      console.log('User found. Current password (first 20 chars):', user.password.substring(0, 20));
      
      // Check if password is already hashed (bcrypt hashes start with $2a$, $2b$, or $2y$)
      const isAlreadyHashed = user.password.startsWith('$2');
      
      if (isAlreadyHashed) {
        console.log('Password is already hashed. Verifying...');
        const isMatch = await user.comparePassword(newPassword);
        if (isMatch) {
          console.log('Password is correct! No update needed.');
        } else {
          console.log('Password is incorrect. Updating...');
          const hashedPassword = await bcrypt.hash(newPassword, 10);
          await User.updateOne({ email }, { password: hashedPassword, updatedAt: new Date() });
          console.log('Password updated successfully!');
        }
      } else {
        console.log('Password is NOT hashed (plain text). Hashing now...');
        const hashedPassword = await bcrypt.hash(newPassword, 10);
        await User.updateOne({ email }, { password: hashedPassword, updatedAt: new Date() });
        console.log('Password hashed and updated successfully!');
      }
    }

    // Verify the password works
    const updatedUser = await User.findOne({ email });
    if (updatedUser) {
      const isMatch = await updatedUser.comparePassword(newPassword);
      console.log('\n=== Verification ===');
      console.log('Password verification:', isMatch ? 'SUCCESS ✓' : 'FAILED ✗');
      console.log('\nUser details:');
      console.log('  - Email:', updatedUser.email);
      console.log('  - Name:', updatedUser.name);
      console.log('  - Role:', updatedUser.role);
      console.log('  - IsActive:', updatedUser.isActive);
      console.log('  - Password hash (first 30 chars):', updatedUser.password.substring(0, 30) + '...');
    } else {
      console.log('ERROR: User not found after update!');
    }

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('Error:', error);
    await mongoose.connection.close();
    process.exit(1);
  }
}

fixAdminPassword();

