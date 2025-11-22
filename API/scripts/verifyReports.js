const { mongoose } = require('../db');
const Report = require('../models/report');
const User = require('../models/user');

async function verifyReports() {
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

    // Get all reports
    const reports = await Report.find({}).populate('reporterId', 'name email role').populate('reportedUserId', 'name email role');
    console.log(`Found ${reports.length} reports in database\n`);

    let validCount = 0;
    let invalidCount = 0;
    const invalidReports = [];

    for (const report of reports) {
      const reporterIdValid = report.reporterId && 
                              mongoose.Types.ObjectId.isValid(report.reporterId._id || report.reporterId);
      const reportedUserIdValid = report.reportedUserId && 
                                 mongoose.Types.ObjectId.isValid(report.reportedUserId._id || report.reportedUserId);

      if (reporterIdValid && reportedUserIdValid) {
        validCount++;
      } else {
        invalidCount++;
        invalidReports.push({
          _id: report._id,
          reporterId: report.reporterId,
          reportedUserId: report.reportedUserId,
          reason: report.reason
        });
      }
    }

    console.log('📊 Verification Results:');
    console.log(`   ✅ Valid reports: ${validCount}`);
    console.log(`   ❌ Invalid reports: ${invalidCount}\n`);

    if (invalidCount > 0) {
      console.log('⚠️  Invalid reports:');
      invalidReports.forEach((r, index) => {
        console.log(`   ${index + 1}. Report ID: ${r._id}`);
        console.log(`      ReporterId: ${r.reporterId} (${typeof r.reporterId})`);
        console.log(`      ReportedUserId: ${r.reportedUserId} (${typeof r.reportedUserId})`);
        console.log(`      Reason: ${r.reason}\n`);
      });
      console.log('💡 Chạy: npm run fix-reports để sửa các reports không hợp lệ');
    } else {
      console.log('✨ Tất cả reports đều hợp lệ!');
      console.log('\n📋 Sample reports:');
      reports.slice(0, 3).forEach((report, index) => {
        console.log(`\n   ${index + 1}. Report: ${report.reason}`);
        console.log(`      Reporter: ${report.reporterId?.name || 'N/A'} (${report.reporterId?.email || 'N/A'})`);
        console.log(`      Reported: ${report.reportedUserId?.name || 'N/A'} (${report.reportedUserId?.email || 'N/A'})`);
        console.log(`      Type: ${report.reportType}, Status: ${report.status}`);
      });
    }

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error.message);
    await mongoose.connection.close();
    process.exit(1);
  }
}

verifyReports();

