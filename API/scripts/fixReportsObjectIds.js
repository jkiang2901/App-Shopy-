const { mongoose } = require('../db');
const Report = require('../models/report');
const User = require('../models/user');

async function fixReportsObjectIds() {
  try {
    // Wait for MongoDB connection
    await new Promise((resolve) => {
      if (mongoose.connection.readyState === 1) {
        resolve();
      } else {
        mongoose.connection.once('open', resolve);
      }
    });

    console.log('Connected to MongoDB\n');

    // Get all users to create a mapping
    const allUsers = await User.find({});
    const userMap = {};
    allUsers.forEach(user => {
      userMap[user.email] = user._id;
      userMap[user.name] = user._id;
    });

    // Get all reports
    const reports = await Report.find({});
    console.log(`Found ${reports.length} reports in database\n`);

    let fixedCount = 0;
    let errorCount = 0;
    const errors = [];

    // Get all customers and sellers
    const customers = await User.find({ role: 'customer' }).limit(10);
    const sellers = await User.find({ role: 'seller' }).limit(10);

    if (customers.length === 0 || sellers.length === 0) {
      console.log('❌ Không tìm thấy customers hoặc sellers trong database');
      console.log(`   Customers: ${customers.length}, Sellers: ${sellers.length}`);
      await mongoose.connection.close();
      process.exit(1);
    }

    console.log(`✅ Tìm thấy ${customers.length} customers và ${sellers.length} sellers\n`);

    // Create mapping for placeholder IDs
    const customerIdMap = {
      'CUSTOMER_ID_1': customers[0]._id,
      'CUSTOMER_ID_2': customers[1] ? customers[1]._id : customers[0]._id,
      'CUSTOMER_ID_3': customers[2] ? customers[2]._id : customers[0]._id,
      'CUSTOMER_ID_4': customers[3] ? customers[3]._id : customers[0]._id,
      'CUSTOMER_ID_5': customers[4] ? customers[4]._id : customers[0]._id,
    };

    const sellerIdMap = {
      'SELLER_ID_1': sellers[0]._id,
      'SELLER_ID_2': sellers[1] ? sellers[1]._id : sellers[0]._id,
      'SELLER_ID_3': sellers[2] ? sellers[2]._id : sellers[0]._id,
      'SELLER_ID_4': sellers[3] ? sellers[3]._id : sellers[0]._id,
      'SELLER_ID_5': sellers[4] ? sellers[4]._id : sellers[0]._id,
    };

    // Process each report
    for (const report of reports) {
      try {
        let needsUpdate = false;
        const updateData = {};

        // Check and fix reporterId
        const reporterIdValue = report.reporterId;
        const isReporterIdValid = reporterIdValue && 
                                  mongoose.Types.ObjectId.isValid(reporterIdValue) && 
                                  typeof reporterIdValue !== 'string';
        
        if (!isReporterIdValid) {
          const reporterIdStr = reporterIdValue?.toString() || '';
          
          // Try to find in customer map (for placeholder IDs like "CUSTOMER_ID_1")
          if (customerIdMap[reporterIdStr]) {
            updateData.reporterId = customerIdMap[reporterIdStr];
            needsUpdate = true;
            console.log(`   🔄 Report ${report._id}: Mapping reporterId "${reporterIdStr}" -> ${customerIdMap[reporterIdStr]}`);
          }
          // Try to find in seller map (for placeholder IDs like "SELLER_ID_1")
          else if (sellerIdMap[reporterIdStr]) {
            updateData.reporterId = sellerIdMap[reporterIdStr];
            needsUpdate = true;
            console.log(`   🔄 Report ${report._id}: Mapping reporterId "${reporterIdStr}" -> ${sellerIdMap[reporterIdStr]}`);
          }
          // Try to find by email or name
          else if (userMap[reporterIdStr]) {
            updateData.reporterId = userMap[reporterIdStr];
            needsUpdate = true;
            console.log(`   🔄 Report ${report._id}: Mapping reporterId "${reporterIdStr}" by email/name`);
          }
          else {
            // Default: assign to first customer or seller based on report type
            if (report.reportType === 'seller') {
              updateData.reporterId = customers[0]._id; // Customer reports seller
            } else {
              updateData.reporterId = sellers[0]._id; // Seller reports buyer
            }
            needsUpdate = true;
            console.log(`   ⚠️  Report ${report._id}: Không tìm thấy reporterId "${reporterIdStr}", gán mặc định (${report.reportType === 'seller' ? 'customer' : 'seller'})`);
          }
        }

        // Check and fix reportedUserId
        const reportedUserIdValue = report.reportedUserId;
        const isReportedUserIdValid = reportedUserIdValue && 
                                     mongoose.Types.ObjectId.isValid(reportedUserIdValue) && 
                                     typeof reportedUserIdValue !== 'string';
        
        if (!isReportedUserIdValid) {
          const reportedUserIdStr = reportedUserIdValue?.toString() || '';
          
          // Try to find in customer map (for placeholder IDs like "CUSTOMER_ID_1")
          if (customerIdMap[reportedUserIdStr]) {
            updateData.reportedUserId = customerIdMap[reportedUserIdStr];
            needsUpdate = true;
            console.log(`   🔄 Report ${report._id}: Mapping reportedUserId "${reportedUserIdStr}" -> ${customerIdMap[reportedUserIdStr]}`);
          }
          // Try to find in seller map (for placeholder IDs like "SELLER_ID_1")
          else if (sellerIdMap[reportedUserIdStr]) {
            updateData.reportedUserId = sellerIdMap[reportedUserIdStr];
            needsUpdate = true;
            console.log(`   🔄 Report ${report._id}: Mapping reportedUserId "${reportedUserIdStr}" -> ${sellerIdMap[reportedUserIdStr]}`);
          }
          // Try to find by email or name
          else if (userMap[reportedUserIdStr]) {
            updateData.reportedUserId = userMap[reportedUserIdStr];
            needsUpdate = true;
            console.log(`   🔄 Report ${report._id}: Mapping reportedUserId "${reportedUserIdStr}" by email/name`);
          }
          else {
            // Default: assign based on report type
            if (report.reportType === 'seller') {
              updateData.reportedUserId = sellers[0]._id; // Customer reports seller
            } else {
              updateData.reportedUserId = customers[0]._id; // Seller reports buyer
            }
            needsUpdate = true;
            console.log(`   ⚠️  Report ${report._id}: Không tìm thấy reportedUserId "${reportedUserIdStr}", gán mặc định (${report.reportType === 'seller' ? 'seller' : 'customer'})`);
          }
        }

        // Update if needed
        if (needsUpdate) {
          updateData.updatedAt = new Date();
          await Report.updateOne(
            { _id: report._id },
            { $set: updateData }
          );
          fixedCount++;
          console.log(`   ✅ Fixed report ${report._id}`);
        }
      } catch (error) {
        errorCount++;
        errors.push({ reportId: report._id, error: error.message });
        console.log(`   ❌ Error fixing report ${report._id}: ${error.message}`);
      }
    }

    console.log(`\n📊 Summary:`);
    console.log(`   ✅ Fixed: ${fixedCount} reports`);
    console.log(`   ❌ Errors: ${errorCount} reports`);
    
    if (errors.length > 0) {
      console.log(`\n⚠️  Errors details:`);
      errors.forEach(({ reportId, error }) => {
        console.log(`   - Report ${reportId}: ${error}`);
      });
    }

    if (fixedCount > 0) {
      console.log(`\n✨ Successfully fixed ${fixedCount} reports!`);
      console.log('   Bạn có thể kiểm tra reports trong Admin interface.');
    } else {
      console.log(`\n✨ Không có reports nào cần sửa. Tất cả đã đúng!`);
    }

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error.message);
    console.error(error.stack);
    await mongoose.connection.close();
    process.exit(1);
  }
}

fixReportsObjectIds();

