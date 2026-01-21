<template>
  <div class="canteen-ordering-container">
    <div class="ordering-header">
      <div class="header-left">
        <el-button
          type="primary"
          size="large"
          :icon="ArrowLeft"
          @click="goBack"
          class="back-button"
        >
          返回
        </el-button>
        <h1 class="ordering-title">食堂智能点餐</h1>
      </div>
      <div class="ordering-info">
        <el-badge :value="cartItems.length" type="danger" :hidden="cartItems.length === 0">
          <el-icon size="24" @click="showCart = !showCart">
            <i-tabler-shopping-cart /> 
          </el-icon>
        </el-badge>
      </div>
    </div>

    <div class="ordering-content">
      <!-- 左侧：菜品展示区 -->
      <div class="menu-section">
        <!-- 菜品分类 -->
        <div class="category-tabs">
          <el-tabs v-model="activeCategory" type="border-card">
            <el-tab-pane
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :name="category.id"
            >
              <!-- 菜品列表 -->
              <div class="dishes-grid">
                <el-card
                  v-for="dish in getDishesByCategory(category.id)"
                  :key="dish.id"
                  class="dish-card"
                  hoverable
                  @click="showDishDetail(dish)"
                >
                  <template #header>
                    <div class="dish-header">
                      <h3 class="dish-name">{{ dish.name }}</h3>
                      <span class="dish-price">¥{{ dish.price.toFixed(2) }}</span>
                    </div>
                  </template>
                  <div class="dish-image">
                    <img 
                      :src="dish.image" 
                      :alt="dish.name" 
                      @error="handleImageError($event, dish)"
                      @load="handleImageLoad($event, dish)"
                      class="dish-img"
                      :style="{ display: dish.image ? 'block' : 'none' }"
                    />
                    <div 
                      class="dish-placeholder" 
                      :style="{ display: dish.image ? 'none' : 'flex' }"
                    >
                      <el-icon size="48" style="color: #909399;"><i-tabler-photo /></el-icon>
                      <span>{{ dish.name }}</span>
                    </div>
                  </div>
                  <div class="dish-footer">
                    <span class="dish-description">{{ dish.description }}</span>
                    <el-button
                      type="primary"
                      size="small"
                      :icon="Plus"
                      @click.stop="addToCart(dish)"
                    >
                      加入购物车
                    </el-button>
                  </div>
                </el-card>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <!-- 右侧：购物车和订单区 -->
      <div class="cart-section" v-show="showCart || cartItems.length > 0">
        <div class="cart-header">
          <h2>购物车</h2>
          <el-button
            v-if="cartItems.length > 0"
            type="danger"
            size="small"
            :icon="Trash"
            @click="clearCart"
          >
            清空购物车
          </el-button>
        </div>

        <!-- 购物车为空 -->
        <div v-if="cartItems.length === 0" class="empty-cart">
          <el-icon size="64" style="color: #909399;"><i-tabler-shopping-cart-off /></el-icon>
          <p>购物车是空的</p>
          <p class="empty-hint">快去选择美味的菜品吧~</p>
        </div>

        <!-- 购物车列表 -->
        <div v-else class="cart-list">
          <!-- 座位和楼层选择 -->
          <div class="seat-selection">
            <h4>座位信息</h4>
            <div class="selection-row">
              <div class="selection-item">
                <span class="selection-label">楼层：</span>
                <el-select 
                  v-model="selectedFloor" 
                  placeholder="选择楼层"
                  clearable
                  style="width: 100px"
                >
                  <el-option 
                    v-for="floor in floors" 
                    :key="floor"
                    :label="`${floor}楼`"
                    :value="floor"
                  />
                </el-select>
              </div>
              <div class="selection-item seat-select-item">
                <span class="selection-label">座位：</span>
                <el-select 
                  v-model="selectedSeatNumber" 
                  placeholder="选择座位号"
                  clearable
                  style="width: 120px"
                >
                  <el-option 
                    v-for="seat in availableSeats" 
                    :key="seat"
                    :label="`座位 ${seat}`"
                    :value="seat"
                  />
                </el-select>
              </div>
            </div>
            <div class="seat-info" v-if="selectedSeatNumber && selectedFloor">
              <span>配送至：{{ selectedFloor }}楼 - 座位 {{ selectedSeatNumber }}</span>
            </div>
          </div>
          
          <!-- 下单人信息 -->
          <div class="orderer-section">
            <h4>下单人信息</h4>
            <el-input 
              v-model="ordererName" 
              placeholder="请输入您的姓名"
              prefix-icon="User"
              clearable
            />
          </div>
          
          <div
            v-for="(item, index) in cartItems"
            :key="index"
            class="cart-item"
          >
            <div class="cart-item-info">
              <h4>{{ item.name }}</h4>
              <span class="cart-item-price">¥{{ item.price.toFixed(2) }}</span>
            </div>
            <div class="cart-item-actions">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="99"
                size="small"
                @change="updateCartItem(item)"
              />
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                circle
                @click="removeFromCart(index)"
              />
            </div>
          </div>

          <!-- 订单总结 -->
          <div class="order-summary">
            <div class="summary-item">
              <span>商品总价：</span>
              <span class="price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span>配送费：</span>
              <span class="price">¥{{ deliveryFee.toFixed(2) }}</span>
            </div>
            <div class="summary-item total">
              <span>总计：</span>
              <span class="total-price">¥{{ (totalPrice + deliveryFee).toFixed(2) }}</span>
            </div>
          </div>

          <!-- 订单操作 -->
          <div class="order-actions">
            <el-button
              type="primary"
              size="large"
              :icon="CreditCard"
              @click="submitOrder"
              :disabled="cartItems.length === 0"
            >
              提交订单
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 菜品详情弹窗 -->
    <el-dialog
      v-model="dishDetailVisible"
      :title="selectedDish?.name || '菜品详情'"
      width="60%"
    >
      <div v-if="selectedDish" class="dish-detail">
          <div class="detail-image">
            <img 
              :src="selectedDish.image" 
              :alt="selectedDish.name" 
              @error="handleImageError($event, selectedDish)"
              @load="handleImageLoad($event, selectedDish)"
              class="dish-img"
              :style="{ display: selectedDish.image ? 'block' : 'none' }"
            />
            <div 
              class="dish-placeholder" 
              :style="{ display: selectedDish.image ? 'none' : 'flex' }"
            >
              <el-icon size="64" style="color: #909399;"><i-tabler-photo /></el-icon>
              <span>{{ selectedDish.name }}</span>
            </div>
          </div>
        <div class="detail-info">
          <h3 class="detail-name">{{ selectedDish.name }}</h3>
          <span class="detail-price">¥{{ selectedDish.price.toFixed(2) }}</span>
          <div class="detail-description">
            <h4>菜品描述</h4>
            <p>{{ selectedDish.description }}</p>
          </div>
          <div class="detail-attributes">
            <div class="attribute-item">
              <el-icon><i-tabler-star /></el-icon>
              <span>评分：{{ selectedDish.rating }}/5</span>
            </div>
            <div class="attribute-item">
              <el-icon><i-tabler-thermometer /></el-icon>
              <span>热量：{{ selectedDish.calories }}kcal</span>
            </div>
            <div class="attribute-item">
              <el-icon><i-tabler-clock /></el-icon>
              <span>制作时间：{{ selectedDish.prepTime }}分钟</span>
            </div>
          </div>
          <div class="detail-actions">
            <el-input-number
              v-model="selectedDishQuantity"
              :min="1"
              :max="99"
              style="margin-right: 10px"
            />
            <el-button
              type="primary"
              size="large"
              :icon="Plus"
              @click="addToCart(selectedDish, selectedDishQuantity)"
            >
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 支付弹窗 -->
    <el-dialog 
      v-model="paymentDialogVisible" 
      title="订单支付" 
      width="550px"
      custom-class="custom-payment-dialog"
    >
      <div class="payment-content">
        <!-- 订单信息卡片 -->
        <div class="payment-card">
          <h3 class="payment-title">
            <el-icon><i-tabler-receipt /></el-icon>
            订单信息
          </h3>
          <div class="payment-order-info">
            <div class="order-info-item">
              <span class="label">订单编号：</span>
              <span class="value">{{ generatedOrderId }}</span>
            </div>
            <div class="order-info-item">
              <span class="label">下单时间：</span>
              <span class="value">{{ new Date().toLocaleString() }}</span>
            </div>
            <div class="order-info-item total-amount">
              <span class="label">支付金额：</span>
              <span class="payment-amount">¥{{ (totalPrice + deliveryFee).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 支付方式选择 -->
        <div class="payment-card">
          <h3 class="payment-title">
            <el-icon><i-tabler-credit-card /></el-icon>
            选择支付方式
          </h3>
          <div class="payment-methods">
            <el-radio-group v-model="selectedPaymentMethod" class="payment-methods-group">
              <el-radio-button label="wechat" class="payment-method-btn">
                <div class="method-item">
                  <el-icon class="method-icon wechat-icon"><i-tabler-brand-wechat /></el-icon>
                  <span>微信支付</span>
                </div>
              </el-radio-button>
              <el-radio-button label="alipay" class="payment-method-btn">
                <div class="method-item">
                  <el-icon class="method-icon alipay-icon"><i-tabler-brand-alipay /></el-icon>
                  <span>支付宝</span>
                </div>
              </el-radio-button>
              <el-radio-button label="card" class="payment-method-btn">
                <div class="method-item">
                  <el-icon class="method-icon card-icon"><i-tabler-credit-card /></el-icon>
                  <span>校园卡</span>
                </div>
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 支付二维码 -->
        <div class="payment-card">
          <h3 class="payment-title">
            <el-icon><i-tabler-qrcode /></el-icon>
            扫描支付
          </h3>
          <div class="payment-qrcode-container">
            <!-- 微信支付二维码 -->
            <div v-if="selectedPaymentMethod === 'wechat'" class="qrcode-wrapper wechat-qrcode">
              <div class="qrcode-scan-box">
                <!-- 3D 中间元素 -->
                <div 
                  class="qrcode-3d-center" 
                  @click="toggleQRCodePopup('wechat')"
                  :class="{ 'animate-3d': true }"
                >
                  <el-icon size="48" class="qrcode-3d-icon"><i-tabler-brand-wechat /></el-icon>
                </div>
                <!-- 网格背景 -->
                <div class="qrcode-scan-grid"></div>
              </div>
              <div class="qrcode-label">微信扫一扫支付</div>
            </div>

            <!-- 支付宝二维码 -->
            <div v-else-if="selectedPaymentMethod === 'alipay'" class="qrcode-wrapper alipay-qrcode">
              <div class="qrcode-scan-box">
                <!-- 3D 中间元素 -->
                <div 
                  class="qrcode-3d-center" 
                  @click="toggleQRCodePopup('alipay')"
                  :class="{ 'animate-3d': true }"
                >
                  <el-icon size="48" class="qrcode-3d-icon"><i-tabler-brand-alipay /></el-icon>
                </div>
                <!-- 网格背景 -->
                <div class="qrcode-scan-grid"></div>
              </div>
              <div class="qrcode-label">支付宝扫一扫支付</div>
            </div>

            <!-- 校园卡二维码 -->
            <div v-else class="qrcode-wrapper card-qrcode">
              <div class="qrcode-scan-box">
                <!-- 3D 中间元素 -->
                <div 
                  class="qrcode-3d-center" 
                  @click="toggleQRCodePopup('card')"
                  :class="{ 'animate-3d': true }"
                >
                  <el-icon size="48" class="qrcode-3d-icon"><i-tabler-building /></el-icon>
                </div>
                <!-- 网格背景 -->
                <div class="qrcode-scan-grid"></div>
              </div>
              <div class="qrcode-label">校园卡扫一扫支付</div>
            </div>
          </div>
        </div>

        <!-- 二维码弹出层 -->
        <div v-if="showQRCodePopup" class="qrcode-popup-overlay" @click="closeQRCodePopup">
          <div class="qrcode-popup-content" @click.stop>
            <div class="qrcode-popup-header">
              <h4>{{ qrCodePopupTitle }}</h4>
              <el-button type="text" @click="closeQRCodePopup" class="popup-close-btn">
                <el-icon><i-tabler-x /></el-icon>
              </el-button>
            </div>
            <div class="qrcode-popup-body">
              <!-- 自定义二维码 -->
              <div class="custom-qrcode" :class="`qrcode-${qrCodePopupType}`">
                <!-- 二维码矩阵 -->
                <div class="qrcode-matrix">
                  <!-- 左上角定位图案 -->
                  <div class="qrcode-position top-left"></div>
                  <!-- 右上角定位图案 -->
                  <div class="qrcode-position top-right"></div>
                  <!-- 左下角定位图案 -->
                  <div class="qrcode-position bottom-left"></div>
                  <!-- 数据模块 -->
                  <div class="qrcode-data"></div>
                  <!-- 中央Logo -->
                  <div class="qrcode-logo">
                    <el-icon size="64" v-if="qrCodePopupType === 'wechat'">
                      <i-tabler-brand-wechat />
                    </el-icon>
                    <el-icon size="64" v-else-if="qrCodePopupType === 'alipay'">
                      <i-tabler-brand-alipay />
                    </el-icon>
                    <el-icon size="64" v-else>
                      <i-tabler-building />
                    </el-icon>
                  </div>
                </div>
              </div>
              <div class="qrcode-popup-tip">请将手机对准二维码进行扫描</div>
            </div>
          </div>
        </div>

        <!-- 支付提示 -->
        <div class="payment-tips">
          <el-icon class="tip-icon"><i-tabler-info-circle /></el-icon>
          <span>请在5分钟内完成支付，超时订单将自动取消</span>
        </div>
      </div>
      <template #footer>
        <div class="payment-actions">
          <el-button @click="paymentDialogVisible = false" class="cancel-btn">取消支付</el-button>
          <el-button type="primary" @click="completePayment" class="confirm-btn">
            <el-icon><i-tabler-check /></el-icon>
            支付完成
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import {
  Plus,
  Delete,
  CreditCard,
  ShoppingCart,
  ArrowLeft
} from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
// 导入记账API
import { saveBill } from '../api/accounting'

// 导入本地图片资源
import gongbaoJiding from '@/assets/宫保鸡丁盖饭.jpg'
import niuRouMian from '@/assets/牛肉面.jpg'
import xianglaJituiBao from '@/assets/香辣鸡腿堡.jpg'
import keLe from '@/assets/可乐.jpg'
import zhaShuTiao from '@/assets/炸薯条.jpg'
import yuXiangRouSi from '@/assets/鱼香肉丝盖饭.jpg'
import xiHongShiJiDanMian from '@/assets/西红柿鸡蛋面.jpg'
import chengZhi from '@/assets/橙汁.jpg'

// 路由
const router = useRouter()
const route = useRoute()

// 状态管理
const activeCategory = ref('category1')
const showCart = ref(true)
const dishDetailVisible = ref(false)
const paymentDialogVisible = ref(false)
const selectedDish = ref(null)
const selectedDishQuantity = ref(1)
const selectedPaymentMethod = ref('wechat') // 生成订单ID
const generatedOrderId = ref('')
// 二维码弹窗状态
const showQRCodePopup = ref(false)
const qrCodePopupType = ref('wechat')
const qrCodePopupTitle = ref('微信支付二维码')

// 座位选择相关状态
const selectedSeatNumber = ref('')
const selectedFloor = ref('')
const seatArea = ref('')
const ordererName = ref('')

// 楼层选项
const floors = ref([1, 2, 3])

// 可用座位列表（模拟数据）
const availableSeats = ref([])
for (let i = 1; i <= 50; i++) {
  availableSeats.value.push(i.toString().padStart(3, '0'))
}

// 配送费
const deliveryFee = ref(2.00)

// 返回按钮点击事件
const goBack = () => {
  router.push('/canteen/seating')
}

// 菜品分类数据
const categories = ref([
  { id: 'category1', name: '热销推荐' },
  { id: 'category2', name: '盖浇饭' },
  { id: 'category3', name: '面条' },
  { id: 'category4', name: '汉堡' },
  { id: 'category5', name: '饮品' },
  { id: 'category6', name: '小吃' }
])

// 菜品数据
const dishes = ref([
  { 
    id: 'dish1', name: '宫保鸡丁盖饭', price: 18.00, categoryId: 'category2', 
    description: '经典川菜，鸡肉鲜嫩，花生香脆', 
    image: gongbaoJiding,
    rating: 4.8, calories: 520, prepTime: 10
  },
  { 
    id: 'dish2', name: '牛肉面', price: 15.00, categoryId: 'category3', 
    description: '牛肉软烂，汤汁浓郁', 
    image: niuRouMian,
    rating: 4.6, calories: 480, prepTime: 8
  },
  { 
    id: 'dish3', name: '香辣鸡腿堡', price: 12.00, categoryId: 'category4', 
    description: '外酥里嫩，香辣可口', 
    image: xianglaJituiBao,
    rating: 4.5, calories: 560, prepTime: 5
  },
  { 
    id: 'dish4', name: '可乐', price: 3.00, categoryId: 'category5', 
    description: '冰镇可乐，清爽解渴', 
    image: keLe,
    rating: 4.2, calories: 140, prepTime: 1
  },
  { 
    id: 'dish5', name: '炸薯条', price: 8.00, categoryId: 'category6', 
    description: '金黄酥脆，口感绝佳', 
    image: zhaShuTiao,
    rating: 4.4, calories: 360, prepTime: 3
  },
  { 
    id: 'dish6', name: '鱼香肉丝盖饭', price: 16.00, categoryId: 'category2', 
    description: '酸甜可口，营养丰富', 
    image: yuXiangRouSi,
    rating: 4.7, calories: 490, prepTime: 9
  },
  { 
    id: 'dish7', name: '西红柿鸡蛋面', price: 12.00, categoryId: 'category3', 
    description: '酸甜开胃，老少皆宜', 
    image: xiHongShiJiDanMian,
    rating: 4.3, calories: 420, prepTime: 7
  },
  { 
    id: 'dish8', name: '橙汁', price: 5.00, categoryId: 'category5', 
    description: '新鲜橙汁，富含维生素', 
    image: chengZhi,
    rating: 4.6, calories: 120, prepTime: 2
  }
])

// 购物车数据
const cartItems = ref([])

// 根据分类获取菜品
const getDishesByCategory = (categoryId) => {
  if (categoryId === 'category1') {
    // 热销推荐：包含特定菜品（如炸薯条）和其他推荐菜品
    // 这里只添加炸薯条到热销推荐，也可以添加更多菜品
    const friedChips = dishes.value.find(dish => dish.id === 'dish5')
    if (friedChips) {
      return [friedChips]
    }
    return []
  }
  return dishes.value.filter(dish => dish.categoryId === categoryId)
}

// 显示菜品详情
const showDishDetail = (dish) => {
  selectedDish.value = dish
  selectedDishQuantity.value = 1
  dishDetailVisible.value = true
}

// 图片加载错误处理
const handleImageError = (event, dish) => {
  console.error('Image load error for dish:', dish.name, 'URL:', event.target.src);
  // 显示占位符，隐藏图片
  event.target.style.display = 'none';
  const placeholder = event.target.nextElementSibling;
  if (placeholder && placeholder.classList.contains('dish-placeholder')) {
    placeholder.style.display = 'flex';
  }
}

// 图片加载成功处理
const handleImageLoad = (event, dish) => {
  console.log('Image loaded successfully:', dish.name, 'URL:', event.target.src);
  // 隐藏占位符，显示图片
  event.target.style.display = 'block';
  const placeholder = event.target.nextElementSibling;
  if (placeholder && placeholder.classList.contains('dish-placeholder')) {
    placeholder.style.display = 'none';
  }
}

// 添加到购物车
const addToCart = (dish, quantity = 1) => {
  const existingItem = cartItems.value.find(item => item.id === dish.id)
  if (existingItem) {
    existingItem.quantity += quantity
  } else {
    cartItems.value.push({
      ...dish,
      quantity
    })
  }
  ElMessage.success('已添加到购物车')
  dishDetailVisible.value = false
}

// 更新购物车商品
const updateCartItem = (item) => {
  // 自动更新，因为使用了v-model
  ElMessage.success('购物车已更新')
}

// 从购物车移除
const removeFromCart = (index) => {
  cartItems.value.splice(index, 1)
  ElMessage.success('已从购物车移除')
}

// 清空购物车
const clearCart = () => {
  cartItems.value = []
  ElMessage.success('购物车已清空')
}

// 计算总价
const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 提交订单
const submitOrder = () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车是空的')
    return
  }
  
  // 生成订单ID
  generatedOrderId.value = 'ORD' + Date.now().toString().slice(-8)
  
  // 显示支付弹窗
  paymentDialogVisible.value = true
  
  ElNotification({
    title: '订单提交成功',
    message: `您的订单号是：${generatedOrderId.value}，请尽快完成支付`,
    type: 'success',
    duration: 5000
  })
}

// 组件加载时从路由参数获取座位信息
onMounted(() => {
  // 从路由参数中获取座位信息
  const seatNumber = route.query.seatNumber
  const area = route.query.area
  const floor = route.query.floor
  
  if (seatNumber) {
    selectedSeatNumber.value = seatNumber
    if (area) {
      seatArea.value = area
    }
    if (floor) {
      selectedFloor.value = parseInt(floor)
    }
    ElMessage.success(`已自动选择座位：${seatNumber} ${area ? `(${area})` : ''}`)
  }
})

// 二维码弹窗控制函数
const toggleQRCodePopup = (type) => {
  showQRCodePopup.value = true
  qrCodePopupType.value = type
  
  // 设置弹窗标题
  if (type === 'wechat') {
    qrCodePopupTitle.value = '微信支付二维码'
  } else if (type === 'alipay') {
    qrCodePopupTitle.value = '支付宝二维码'
  } else {
    qrCodePopupTitle.value = '校园卡支付二维码'
  }
}

const closeQRCodePopup = () => {
  showQRCodePopup.value = false
}

// 完成支付
const completePayment = async () => {
  paymentDialogVisible.value = false
  
  // 确定下单人姓名（使用输入的姓名或生成随机ID）
  const displayOrderer = ordererName.value || `用户${Math.floor(Math.random() * 1000).toString().padStart(4, '0')}`
  
  // 保存当前购物车数据用于记账
  const orderItems = [...cartItems.value]
  
  // 保存座位信息副本（在清空之前）
  const savedSeatNumber = selectedSeatNumber.value
  const savedFloor = selectedFloor.value
  
  // 持久化座位信息到 localStorage，供选座页面显示
  if (savedSeatNumber && savedFloor) {
    const bookingInfo = {
      seatNumber: savedSeatNumber,
      floor: savedFloor,
      bookingTime: new Date().toLocaleString()
    }
    localStorage.setItem('canteen_current_booking', JSON.stringify(bookingInfo))
    console.log('座位信息已保存到 localStorage:', bookingInfo)
  }
  
  // 清空购物车
  cartItems.value = []
  
  // 清空表单
  ordererName.value = ''
  selectedSeatNumber.value = ''
  selectedFloor.value = ''
  
  // 同步记账：将每个菜品作为一条账单记录
      try {
        for (const item of orderItems) {
          // 调用记账API保存账单记录
          await saveBill({
            amount: Number(item.price * item.quantity), // 确保是数值类型
            categoryId: 6, // 默认分类：餐饮美食（支出分类第一个，ID=6）
            type: 2, // 支出类型：2=支出
            billDate: new Date().toISOString().split('T')[0],
            remark: `食堂点餐 - ${item.name}，订单号：${generatedOrderId.value}，数量：${item.quantity}`
          })
        }
    
    // 显示支付成功通知和消息（使用保存的副本）
    ElNotification({
      title: '支付成功',
      message: `您的订单已支付成功，餐品将送到${savedFloor ? `${savedFloor}楼座位 ${savedSeatNumber}` : '取餐区'}，下单人：${displayOrderer}`,
      type: 'success',
      duration: 5000
    })
    
    ElMessage.success('支付成功！订单已同步到记账系统')
  } catch (error) {
    console.error('同步记账失败:', error)
    // 即使记账失败，也显示支付成功消息，不影响主流程
    ElNotification({
      title: '支付成功',
      message: `您的订单已支付成功，餐品将送到${savedFloor ? `${savedFloor}楼座位 ${savedSeatNumber}` : '取餐区'}，下单人：${displayOrderer}`,
      type: 'success',
      duration: 5000
    })
    
    ElMessage.success('支付成功！部分订单同步记账失败，请手动添加')
  }
}
</script>

<style scoped>
.canteen-ordering-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.ordering-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
  animation: fadeInLeft 0.5s ease;
}

.back-button {
  transition: all 0.3s ease;
}

.back-button:hover {
  transform: translateX(-5px);
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.ordering-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.ordering-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.ordering-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 30px;
}

.menu-section {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.category-tabs {
  height: 100%;
}

.category-tabs .el-tabs__header {
  margin-bottom: 20px;
}

.dishes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.dish-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  background-color: #ffffff;
}

.dish-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.dish-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.dish-image {
  margin: 15px 0;
  /* 移除固定高度，让容器随图片内容变化 */
  /* height: 180px; */
  overflow: hidden;
  border-radius: 8px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 1px solid #e4e7ed;
  /* 最小高度，确保占位符显示正常 */
  min-height: 120px;
}

.dish-img {
  /* width: 100%; */
  /* height: 100%; */
  /* object-fit: cover; */
  /* 让图片按原始比例显示，不拉伸 */
  max-width: 100%;
  max-height: 280px;
  object-fit: contain;
  transition: transform 0.3s ease, opacity 0.3s ease;
  opacity: 1;
  display: block;
}

.dish-image img.default-image {
  background-color: #fafafa;
  opacity: 0.8;
}

.dish-card:hover .dish-img {
  transform: scale(1.1);
}

.dish-placeholder {
  display: none;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  text-align: center;
  padding: 20px;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ffffff;
  border-radius: 8px;
}

.dish-placeholder span {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 10px;
}

.dish-description {
  font-size: 14px;
  color: #606266;
  margin: 0;
  flex: 1;
  margin-right: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.cart-section {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  height: fit-content;
  position: sticky;
  top: 20px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 40px 0;
  color: #909399;
}

.empty-hint {
  font-size: 14px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.seat-selection {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.selection-row {
  display: flex;
  gap: 15px;
  align-items: center;
}

.selection-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.seat-select-item {
  flex: 1;
}

.selection-label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.seat-selection h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.seat-info {
  margin-top: 10px;
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.orderer-section {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.orderer-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.seat-info {
  margin-top: 10px;
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.cart-item-info h4 {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 5px 0;
}

.cart-item-price {
  font-size: 14px;
  color: #f56c6c;
  font-weight: bold;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-summary {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
}

.summary-item .price {
  font-weight: bold;
  color: #333;
}

.summary-item.total {
  font-size: 18px;
  font-weight: bold;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
}

.order-actions {
  margin-top: 10px;
}

.dish-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.detail-image {
  /* 移除固定高度，让容器随图片内容变化 */
  /* height: 300px; */
  overflow: hidden;
  border-radius: 12px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 1px solid #e4e7ed;
  /* 最小高度，确保占位符显示正常 */
  min-height: 200px;
}

.detail-image img {
  /* width: 100%; */
  /* height: 100%; */
  /* object-fit: cover; */
  /* 让图片按原始比例显示，不拉伸 */
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  display: block;
}

.detail-image .dish-placeholder {
  display: none;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ffffff;
  border-radius: 12px;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-name {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.detail-price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.detail-description h4 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.detail-description p {
  font-size: 15px;
  color: #606266;
  line-height: 1.6;
}

.detail-attributes {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.attribute-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: #606266;
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.custom-payment-dialog {
  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px 12px 0 0;
    .el-dialog__title {
      color: #fff;
      font-size: 18px;
      font-weight: bold;
    }
    .el-dialog__headerbtn {
      .el-dialog__close {
        color: #fff;
        font-size: 20px;
        &:hover {
          background-color: rgba(255, 255, 255, 0.2);
        }
      }
    }
  }
  .el-dialog__body {
    padding: 20px;
    background: #f8f9fa;
  }
  .el-dialog__footer {
    background: #f8f9fa;
    border-radius: 0 0 12px 12px;
    border-top: 1px solid #e9ecef;
  }
}

.payment-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 支付卡片样式 */
.payment-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  &:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }
}

/* 标题样式 */
.payment-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
  .el-icon {
    font-size: 18px;
    color: #667eea;
  }
}

/* 订单信息样式 */
.payment-order-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  .label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }
  .value {
    font-size: 14px;
    color: #333;
    font-weight: 500;
  }
  &.total-amount {
    padding-top: 15px;
    border-top: 1px dashed #e0e0e0;
    .label {
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.payment-amount {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
  text-shadow: 0 2px 4px rgba(245, 108, 108, 0.2);
}

/* 支付方式样式 */
.payment-methods {
  margin: 0;
}

.payment-methods-group {
  width: 100%;
  .el-radio-button:first-child .el-radio-button__inner {
    border-radius: 8px 0 0 8px;
  }
  .el-radio-button:last-child .el-radio-button__inner {
    border-radius: 0 8px 8px 0;
  }
}

.payment-method-btn {
  flex: 1;
  .method-item {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px 0;
    .method-icon {
      font-size: 18px;
      &.wechat-icon {
        color: #07c160;
      }
      &.alipay-icon {
        color: #1677ff;
      }
      &.card-icon {
        color: #ff9500;
      }
    }
    span {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

/* 二维码容器样式 */
.payment-qrcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}

.qrcode-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  width: 100%;
  max-width: 300px;
}

.qrcode-placeholder {
  position: relative;
  width: 250px;
  height: 250px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s ease;
  &:hover {
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
    transform: scale(1.02);
  }
}

/* 二维码网格背景 */
.qrcode-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(90deg, rgba(0, 0, 0, 0.05) 1px, transparent 1px),
    linear-gradient(rgba(0, 0, 0, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: center;
}

/* 二维码图案样式 */
.qrcode-pattern {
  position: absolute;
  width: 80%;
  height: 80%;
  border-radius: 8px;
  &.wechat-pattern {
    background: linear-gradient(135deg, #07c160 0%, #00a854 100%);
    background-image: 
      radial-gradient(circle at 20% 20%, #fff 10px, transparent 10px),
      radial-gradient(circle at 80% 20%, #fff 8px, transparent 8px),
      radial-gradient(circle at 20% 80%, #fff 6px, transparent 6px),
      radial-gradient(circle at 80% 80%, #fff 12px, transparent 12px),
      radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.1) 60%, transparent 60%);
    background-size: 100% 100%;
    background-position: center;
  }
  &.alipay-pattern {
    background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
    background-image: 
      radial-gradient(circle at 25% 25%, #fff 12px, transparent 12px),
      radial-gradient(circle at 75% 25%, #fff 10px, transparent 10px),
      radial-gradient(circle at 25% 75%, #fff 8px, transparent 8px),
      radial-gradient(circle at 75% 75%, #fff 14px, transparent 14px),
      radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.1) 55%, transparent 55%);
    background-size: 100% 100%;
    background-position: center;
  }
  &.card-pattern {
    background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
    background-image: 
      radial-gradient(circle at 30% 30%, #fff 11px, transparent 11px),
      radial-gradient(circle at 70% 30%, #fff 9px, transparent 9px),
      radial-gradient(circle at 30% 70%, #fff 7px, transparent 7px),
      radial-gradient(circle at 70% 70%, #fff 13px, transparent 13px),
      radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.1) 58%, transparent 58%);
    background-size: 100% 100%;
    background-position: center;
  }
}

/* 二维码中心图标样式 */
.qrcode-center {
  position: absolute;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1;
  &.wechat-center {
    .el-icon {
      color: #07c160;
    }
  }
  &.alipay-center {
    .el-icon {
      color: #1677ff;
    }
  }
  &.card-center {
    .el-icon {
      color: #ff9500;
    }
  }
}

/* 二维码标签 */
.qrcode-label {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  text-align: center;
  padding: 10px 20px;
  background: #f0f0f0;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  &:hover {
    background: #e0e0e0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }
}

/* 3D 扫描支付区域样式 */
.qrcode-scan-box {
  position: relative;
  width: 250px;
  height: 250px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s ease;
  &:hover {
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }
}

/* 扫描网格背景 */
.qrcode-scan-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(90deg, rgba(0, 0, 0, 0.05) 1px, transparent 1px),
    linear-gradient(rgba(0, 0, 0, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: center;
  opacity: 0.7;
  /* 允许点击事件穿透到下面的元素 */
  pointer-events: none;
}

/* 3D 中间元素样式 */
.qrcode-3d-center {
  position: relative;
  z-index: 10;
  width: 100px;
  height: 100px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 8px 16px rgba(0, 0, 0, 0.1),
    inset 0 2px 4px rgba(255, 255, 255, 0.8),
    inset 0 -2px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  transform-style: preserve-3d;
  
  &:hover {
    transform: scale(1.1) rotateY(5deg) rotateX(5deg);
    box-shadow: 
      0 12px 24px rgba(0, 0, 0, 0.15),
      inset 0 2px 4px rgba(255, 255, 255, 0.8),
      inset 0 -2px 4px rgba(0, 0, 0, 0.05);
  }
  
  /* 3D 背景效果 */
  &::before {
    content: '';
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    transform: translateZ(-10px);
    transition: all 0.3s ease;
  }
}

/* 3D 图标的样式 */
.qrcode-3d-icon {
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  
  /* 不同支付方式的图标颜色 */
  .qrcode-wrapper.wechat-qrcode & {
    color: #07c160;
  }
  
  .qrcode-wrapper.alipay-qrcode & {
    color: #1677ff;
  }
  
  .qrcode-wrapper.card-qrcode & {
    color: #ff9500;
  }
}

/* 3D 动画效果 */
@keyframes rotate3d {
  0% {
    transform: rotateY(0deg) rotateX(0deg) translateZ(0);
  }
  33% {
    transform: rotateY(10deg) rotateX(5deg) translateZ(10px);
  }
  66% {
    transform: rotateY(-10deg) rotateX(-5deg) translateZ(10px);
  }
  100% {
    transform: rotateY(0deg) rotateX(0deg) translateZ(0);
  }
}

.animate-3d {
  animation: rotate3d 8s ease-in-out infinite;
}

/* 二维码弹窗样式 */
.qrcode-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

.qrcode-popup-content {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: scaleIn 0.3s ease;
}

.qrcode-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 20px;
  h4 {
    margin: 0;
    font-size: 18px;
    font-weight: bold;
    color: #333;
  }
  .popup-close-btn {
    padding: 0;
    margin: 0;
    font-size: 20px;
    color: #666;
    &:hover {
      color: #333;
    }
  }
}

.qrcode-popup-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* 自定义二维码样式 - 3D效果 */
.custom-qrcode {
  width: 300px;
  height: 300px;
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  transform-style: preserve-3d;
  transition: all 0.3s ease;
  animation: rotate3d 8s ease-in-out infinite;
  
  /* 不同支付方式的背景色 */
  &.qrcode-wechat {
    background: linear-gradient(135deg, rgba(7, 193, 96, 0.1) 0%, rgba(0, 168, 84, 0.1) 100%);
    box-shadow: 
      0 8px 24px rgba(7, 193, 96, 0.2),
      inset 0 2px 4px rgba(255, 255, 255, 0.8),
      inset 0 -2px 4px rgba(7, 193, 96, 0.1);
  }
  
  &.qrcode-alipay {
    background: linear-gradient(135deg, rgba(22, 119, 255, 0.1) 0%, rgba(9, 88, 217, 0.1) 100%);
    box-shadow: 
      0 8px 24px rgba(22, 119, 255, 0.2),
      inset 0 2px 4px rgba(255, 255, 255, 0.8),
      inset 0 -2px 4px rgba(22, 119, 255, 0.1);
  }
  
  &.qrcode-card {
    background: linear-gradient(135deg, rgba(255, 149, 0, 0.1) 0%, rgba(255, 107, 0, 0.1) 100%);
    box-shadow: 
      0 8px 24px rgba(255, 149, 0, 0.2),
      inset 0 2px 4px rgba(255, 255, 255, 0.8),
      inset 0 -2px 4px rgba(255, 149, 0, 0.1);
  }
  
  &:hover {
    animation-play-state: paused;
    transform: scale(1.05) rotateY(10deg) rotateX(5deg);
  }
}

/* 二维码矩阵 - 3D效果 */
.qrcode-matrix {
  width: 250px;
  height: 250px;
  position: relative;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transform-style: preserve-3d;
  transform: translateZ(10px);
  box-shadow: 
    0 12px 30px rgba(0, 0, 0, 0.15),
    inset 0 2px 4px rgba(255, 255, 255, 0.8),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1);
  
  /* 3D底部阴影效果 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.03);
    transform: translateZ(-5px) scale(0.95);
    border-radius: 8px;
    z-index: -1;
  }
  
  /* 真实的二维码像素点 */
  .qrcode-position {
    background: #000 !important;
  }
  
  /* 生成真实的二维码图案 - 使用多个小的圆形点 */
  .qrcode-data {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: 
      /* 生成二维码的黑白点阵图案 */
      linear-gradient(90deg, #000 2px, transparent 2px),
      linear-gradient(#000 2px, transparent 2px),
      /* 二维码数据点 - 模拟真实二维码的像素点 */
      radial-gradient(circle at 30px 30px, #000 3px, transparent 3px),
      radial-gradient(circle at 70px 30px, #000 3px, transparent 3px),
      radial-gradient(circle at 110px 30px, #000 3px, transparent 3px),
      radial-gradient(circle at 150px 30px, #000 3px, transparent 3px),
      radial-gradient(circle at 190px 30px, #000 3px, transparent 3px),
      radial-gradient(circle at 230px 30px, #000 3px, transparent 3px),
      
      radial-gradient(circle at 30px 70px, #000 3px, transparent 3px),
      radial-gradient(circle at 70px 70px, #000 3px, transparent 3px),
      radial-gradient(circle at 110px 70px, #000 3px, transparent 3px),
      radial-gradient(circle at 150px 70px, #000 3px, transparent 3px),
      radial-gradient(circle at 190px 70px, #000 3px, transparent 3px),
      radial-gradient(circle at 230px 70px, #000 3px, transparent 3px),
      
      radial-gradient(circle at 30px 110px, #000 3px, transparent 3px),
      radial-gradient(circle at 70px 110px, #000 3px, transparent 3px),
      radial-gradient(circle at 110px 110px, #000 3px, transparent 3px),
      radial-gradient(circle at 150px 110px, #000 3px, transparent 3px),
      radial-gradient(circle at 190px 110px, #000 3px, transparent 3px),
      radial-gradient(circle at 230px 110px, #000 3px, transparent 3px),
      
      radial-gradient(circle at 30px 150px, #000 3px, transparent 3px),
      radial-gradient(circle at 70px 150px, #000 3px, transparent 3px),
      radial-gradient(circle at 110px 150px, #000 3px, transparent 3px),
      radial-gradient(circle at 150px 150px, #000 3px, transparent 3px),
      radial-gradient(circle at 190px 150px, #000 3px, transparent 3px),
      radial-gradient(circle at 230px 150px, #000 3px, transparent 3px),
      
      radial-gradient(circle at 30px 190px, #000 3px, transparent 3px),
      radial-gradient(circle at 70px 190px, #000 3px, transparent 3px),
      radial-gradient(circle at 110px 190px, #000 3px, transparent 3px),
      radial-gradient(circle at 150px 190px, #000 3px, transparent 3px),
      radial-gradient(circle at 190px 190px, #000 3px, transparent 3px),
      radial-gradient(circle at 230px 190px, #000 3px, transparent 3px),
      
      radial-gradient(circle at 30px 230px, #000 3px, transparent 3px),
      radial-gradient(circle at 70px 230px, #000 3px, transparent 3px),
      radial-gradient(circle at 110px 230px, #000 3px, transparent 3px),
      radial-gradient(circle at 150px 230px, #000 3px, transparent 3px),
      radial-gradient(circle at 190px 230px, #000 3px, transparent 3px),
      radial-gradient(circle at 230px 230px, #000 3px, transparent 3px);
    
    background-size: 40px 40px, 40px 40px, 40px 40px;
    background-position: center;
    transform: translateZ(2px);
  }
}

/* 二维码定位图案 - 3D效果 */
.qrcode-position {
  position: absolute;
  width: 40px;
  height: 40px;
  background: #000;
  border-radius: 4px;
  transform: translateZ(5px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  
  /* 定位图案的 3 层嵌套 */
  &::before {
    content: '';
    position: absolute;
    top: 6px;
    left: 6px;
    width: 20px;
    height: 20px;
    background: #fff;
    border-radius: 2px;
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  &::after {
    content: '';
    position: absolute;
    top: 11px;
    left: 11px;
    width: 10px;
    height: 10px;
    background: #000;
    border-radius: 1px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }
  
  &.top-left {
    top: 10px;
    left: 10px;
    transform: translateZ(5px) rotateY(5deg) rotateX(5deg);
  }
  
  &.top-right {
    top: 10px;
    right: 10px;
    transform: translateZ(5px) rotateY(-5deg) rotateX(5deg);
  }
  
  &.bottom-left {
    bottom: 10px;
    left: 10px;
    transform: translateZ(5px) rotateY(5deg) rotateX(-5deg);
  }
}

/* 二维码数据模块 - 优化显示效果 */
.qrcode-data {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    /* 网格背景 */
    linear-gradient(90deg, rgba(0, 0, 0, 0.05) 1px, transparent 1px),
    linear-gradient(rgba(0, 0, 0, 0.05) 1px, transparent 1px),
    /* 随机二维码图案 - 更明显的点 */
    radial-gradient(circle at 50px 50px, #000 4px, transparent 4px),
    radial-gradient(circle at 90px 50px, #000 4px, transparent 4px),
    radial-gradient(circle at 130px 50px, #000 4px, transparent 4px),
    radial-gradient(circle at 170px 50px, #000 4px, transparent 4px),
    radial-gradient(circle at 210px 50px, #000 4px, transparent 4px),
    
    radial-gradient(circle at 50px 90px, #000 4px, transparent 4px),
    radial-gradient(circle at 90px 90px, transparent 4px, #000 4px),
    radial-gradient(circle at 130px 90px, #000 4px, transparent 4px),
    radial-gradient(circle at 170px 90px, transparent 4px, #000 4px),
    radial-gradient(circle at 210px 90px, #000 4px, transparent 4px),
    
    radial-gradient(circle at 50px 130px, transparent 4px, #000 4px),
    radial-gradient(circle at 90px 130px, #000 4px, transparent 4px),
    radial-gradient(circle at 130px 130px, #000 4px, transparent 4px),
    radial-gradient(circle at 170px 130px, transparent 4px, #000 4px),
    radial-gradient(circle at 210px 130px, #000 4px, transparent 4px),
    
    radial-gradient(circle at 50px 170px, #000 4px, transparent 4px),
    radial-gradient(circle at 90px 170px, transparent 4px, #000 4px),
    radial-gradient(circle at 130px 170px, #000 4px, transparent 4px),
    radial-gradient(circle at 170px 170px, #000 4px, transparent 4px),
    radial-gradient(circle at 210px 170px, transparent 4px, #000 4px),
    
    radial-gradient(circle at 50px 210px, transparent 4px, #000 4px),
    radial-gradient(circle at 90px 210px, #000 4px, transparent 4px),
    radial-gradient(circle at 130px 210px, transparent 4px, #000 4px),
    radial-gradient(circle at 170px 210px, #000 4px, transparent 4px),
    radial-gradient(circle at 210px 210px, transparent 4px, #000 4px);
  
  background-size: 20px 20px, 20px 20px, 40px 40px;
  background-position: center, center, center;
  transform: translateZ(2px);
  opacity: 0.8;
}

/* 优化二维码矩阵背景色 */
.qrcode-matrix {
  background: #fff !important;
  /* 确保二维码矩阵是白色背景 */
}

/* 优化二维码图案 */
.custom-qrcode {
  /* 优化二维码背景样式 */
  background: #ffffff !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid rgba(0, 0, 0, 0.05) !important;
}

/* 二维码中央Logo - 3D效果 */
.qrcode-logo {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) translateZ(15px);
  width: 80px;
  height: 80px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 12px 24px rgba(0, 0, 0, 0.2),
    inset 0 2px 4px rgba(255, 255, 255, 0.8),
    inset 0 -2px 4px rgba(0, 0, 0, 0.1);
  z-index: 1;
  transition: all 0.3s ease;
  animation: pulse 2s ease-in-out infinite;
  
  /* 3D底部阴影效果 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.1);
    transform: translateZ(-5px) scale(0.9);
    border-radius: 50%;
    z-index: -1;
  }
  
  /* 不同支付方式的Logo颜色 */
  .qrcode-wechat & {
    .el-icon {
      color: #07c160;
      text-shadow: 0 2px 4px rgba(7, 193, 96, 0.3);
    }
  }
  
  .qrcode-alipay & {
    .el-icon {
      color: #1677ff;
      text-shadow: 0 2px 4px rgba(22, 119, 255, 0.3);
    }
  }
  
  .qrcode-card & {
    .el-icon {
      color: #ff9500;
      text-shadow: 0 2px 4px rgba(255, 149, 0, 0.3);
    }
  }
}

/* 3D旋转动画 */
@keyframes rotate3d {
  0% {
    transform: rotateY(0deg) rotateX(0deg) translateZ(0);
  }
  33% {
    transform: rotateY(10deg) rotateX(5deg) translateZ(5px);
  }
  66% {
    transform: rotateY(-10deg) rotateX(-5deg) translateZ(5px);
  }
  100% {
    transform: rotateY(0deg) rotateX(0deg) translateZ(0);
  }
}

/* 脉冲动画 */
@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) translateZ(15px) scale(1);
  }
  50% {
    transform: translate(-50%, -50%) translateZ(15px) scale(1.05);
  }
  100% {
    transform: translate(-50%, -50%) translateZ(15px) scale(1);
  }
}

.qrcode-popup-tip {
  font-size: 16px;
  font-weight: 500;
  color: #666;
  text-align: center;
  padding: 10px 20px;
  background: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

/* 支付提示样式 */
.payment-tips {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff3cd;
  border: 1px solid #ffeeba;
  border-radius: 8px;
  padding: 12px 15px;
  margin-top: 10px;
  .tip-icon {
    color: #856404;
    font-size: 16px;
  }
  span {
    font-size: 13px;
    color: #856404;
    font-weight: 500;
  }
}

/* 按钮样式 */
.payment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  .cancel-btn {
    padding: 8px 20px;
    border-radius: 8px;
    font-weight: 500;
    transition: all 0.3s ease;
    &:hover {
      background: #f0f0f0;
      color: #333;
    }
  }
  .confirm-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 24px;
    border-radius: 8px;
    font-weight: bold;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    transition: all 0.3s ease;
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
    }
    .el-icon {
      font-size: 16px;
    }
  }
}

@media (max-width: 1200px) {
  .ordering-content {
    grid-template-columns: 1fr;
  }
  
  .cart-section {
    position: static;
  }
}

@media (max-width: 768px) {
  .dishes-grid {
    grid-template-columns: 1fr;
  }
  
  .dish-detail {
    grid-template-columns: 1fr;
  }
  
  .detail-image {
    height: 200px;
  }
}
</style>