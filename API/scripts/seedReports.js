const { mongoose } = require('../db');
const Report = require('../models/report');
const User = require('../models/user');

async function seedReports() {
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

    // Get users
    const customers = await User.find({ role: 'customer' }).limit(5);
    const sellers = await User.find({ role: 'seller' }).limit(5);

    if (customers.length < 2 || sellers.length < 2) {
      console.log('❌ Cần ít nhất 2 customers và 2 sellers để tạo reports');
      console.log(`Hiện có: ${customers.length} customers, ${sellers.length} sellers\n`);
      console.log('Vui lòng tạo thêm users trước khi chạy script này.');
      await mongoose.connection.close();
      process.exit(1);
    }

    console.log(`✅ Tìm thấy ${customers.length} customers và ${sellers.length} sellers\n`);

    // Sample reports data with actual ObjectIds
    const reportsData = [
      // Customer reports seller (5 reports)
      {
        reporterId: customers[0]._id,
        reportedUserId: sellers[0]._id,
        reportType: 'seller',
        reason: 'Sản phẩm không đúng mô tả',
        description: 'Tôi đã mua sản phẩm nhưng khi nhận hàng thì không giống với mô tả trên website. Chất lượng kém hơn nhiều so với hình ảnh.',
        status: 'pending',
        createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
        updatedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000)
      },
      {
        reporterId: customers[1]._id,
        reportedUserId: sellers[0]._id,
        reportType: 'seller',
        reason: 'Giao hàng chậm trễ',
        description: 'Đơn hàng đã quá hạn giao hàng 3 ngày nhưng vẫn chưa nhận được. Không có phản hồi từ người bán.',
        status: 'reviewing',
        adminNotes: 'Đang liên hệ với người bán để xác minh',
        createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000), // 5 days ago
        updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000) // 1 day ago
      },
      {
        reporterId: customers[2] ? customers[2]._id : customers[0]._id,
        reportedUserId: sellers[1] ? sellers[1]._id : sellers[0]._id,
        reportType: 'seller',
        reason: 'Hàng bị hỏng khi nhận',
        description: 'Sản phẩm bị vỡ khi nhận hàng. Có vẻ như đóng gói không cẩn thận. Yêu cầu hoàn tiền hoặc đổi hàng mới.',
        status: 'resolved',
        adminNotes: 'Đã xử lý: Người bán đã đồng ý hoàn tiền và gửi lại sản phẩm mới',
        createdAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000), // 10 days ago
        updatedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000) // 3 days ago
      },
      {
        reporterId: customers[0]._id,
        reportedUserId: sellers[1] ? sellers[1]._id : sellers[0]._id,
        reportType: 'seller',
        reason: 'Thái độ phục vụ kém',
        description: 'Khi liên hệ hỏi về sản phẩm, người bán trả lời rất thô lỗ và không chuyên nghiệp. Không muốn mua hàng từ shop này nữa.',
        status: 'pending',
        createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000), // 1 day ago
        updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
      },
      {
        reporterId: customers[1]._id,
        reportedUserId: sellers[2] ? sellers[2]._id : (sellers[1] ? sellers[1]._id : sellers[0]._id),
        reportType: 'seller',
        reason: 'Giá cả không minh bạch',
        description: 'Giá trên website khác với giá thực tế khi thanh toán. Có phí ẩn không được thông báo trước.',
        status: 'dismissed',
        adminNotes: 'Đã kiểm tra: Giá đúng với chính sách công khai. Báo cáo không có cơ sở.',
        createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), // 7 days ago
        updatedAt: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000) // 4 days ago
      },
      // Seller reports buyer (5 reports)
      {
        reporterId: sellers[0]._id,
        reportedUserId: customers[0]._id,
        reportType: 'buyer',
        reason: 'Khách hàng không nhận hàng',
        description: 'Khách hàng đã đặt hàng nhưng từ chối nhận hàng khi shipper giao đến. Gây thiệt hại về chi phí vận chuyển.',
        status: 'pending',
        createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3 days ago
        updatedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
      },
      {
        reporterId: sellers[1] ? sellers[1]._id : sellers[0]._id,
        reportedUserId: customers[1]._id,
        reportType: 'buyer',
        reason: 'Đánh giá sai sự thật',
        description: 'Khách hàng để lại đánh giá 1 sao với nội dung không đúng sự thật, ảnh hưởng đến uy tín shop.',
        status: 'reviewing',
        adminNotes: 'Đang xem xét đánh giá và phản hồi từ khách hàng',
        createdAt: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000), // 6 days ago
        updatedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000) // 2 days ago
      },
      {
        reporterId: sellers[0]._id,
        reportedUserId: customers[2] ? customers[2]._id : customers[0]._id,
        reportType: 'buyer',
        reason: 'Yêu cầu hoàn tiền không hợp lý',
        description: 'Khách hàng yêu cầu hoàn tiền sau khi đã sử dụng sản phẩm được 1 tuần. Sản phẩm không có lỗi gì.',
        status: 'resolved',
        adminNotes: 'Đã xử lý: Giải thích chính sách đổi trả cho khách hàng. Không chấp nhận yêu cầu hoàn tiền.',
        createdAt: new Date(Date.now() - 8 * 24 * 60 * 60 * 1000), // 8 days ago
        updatedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000) // 5 days ago
      },
      {
        reporterId: sellers[1] ? sellers[1]._id : sellers[0]._id,
        reportedUserId: customers[0]._id,
        reportType: 'buyer',
        reason: 'Hành vi spam đặt hàng',
        description: 'Khách hàng đặt nhiều đơn hàng rồi hủy liên tục, có vẻ như đang spam hệ thống.',
        status: 'pending',
        createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000), // 1 day ago
        updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
      },
      {
        reporterId: sellers[2] ? sellers[2]._id : (sellers[1] ? sellers[1]._id : sellers[0]._id),
        reportedUserId: customers[1]._id,
        reportType: 'buyer',
        reason: 'Thanh toán không đúng hạn',
        description: 'Khách hàng đã nhận hàng nhưng chưa thanh toán đúng hạn. Đã nhắc nhở nhiều lần nhưng không phản hồi.',
        status: 'dismissed',
        adminNotes: 'Đã xử lý: Khách hàng đã thanh toán đầy đủ. Báo cáo đã được giải quyết.',
        createdAt: new Date(Date.now() - 12 * 24 * 60 * 60 * 1000), // 12 days ago
        updatedAt: new Date(Date.now() - 9 * 24 * 60 * 60 * 1000) // 9 days ago
      }
    ];

    // Check if reports already exist (optional - to avoid duplicates)
    const existingCount = await Report.countDocuments({});
    if (existingCount > 0) {
      console.log(`⚠️  Đã có ${existingCount} reports trong database.`);
      console.log('Bạn có muốn xóa tất cả reports cũ và tạo mới? (y/n)');
      // For automated script, we'll skip existing reports
      console.log('Skipping... (để xóa và tạo mới, hãy chạy: node scripts/clearAndSeedReports.js)\n');
    }

    // Insert reports
    const insertedReports = await Report.insertMany(reportsData, { ordered: false });
    
    console.log(`✅ Successfully created ${insertedReports.length} reports!\n`);
    console.log('📊 Reports summary:');
    console.log(`   - Customer reports seller: ${reportsData.filter(r => r.reportType === 'seller').length}`);
    console.log(`   - Seller reports buyer: ${reportsData.filter(r => r.reportType === 'buyer').length}`);
    console.log(`\n📈 Status breakdown:`);
    const statusCount = {};
    reportsData.forEach(r => {
      statusCount[r.status] = (statusCount[r.status] || 0) + 1;
    });
    Object.entries(statusCount).forEach(([status, count]) => {
      const statusEmoji = {
        'pending': '⏳',
        'reviewing': '🔍',
        'resolved': '✅',
        'dismissed': '❌'
      }[status] || '📌';
      console.log(`   ${statusEmoji} ${status}: ${count}`);
    });

    console.log('\n✨ Done! Bạn có thể kiểm tra reports trong Admin interface.');

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error.message);
    if (error.code === 11000) {
      console.error('   Duplicate key error - một số reports đã tồn tại.');
    }
    await mongoose.connection.close();
    process.exit(1);
  }
}

seedReports();

