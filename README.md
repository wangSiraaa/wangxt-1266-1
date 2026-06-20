# 企业发票风险协查系统

> 一套完整的企业发票风险协查管理系统，支持税务专员标记风险、采购负责人补充材料、财务经理审批结论，供应商黑名单联动冻结报销等完整业务流程。

---

## 一、系统概览

### 1.1 技术栈

| 层级 | 技术选型 |
| --- | --- |
| 前端 | React 18 + TypeScript + Vite + Arco Design + React Router v6 + Axios |
| 后端 | Spring Boot 3.2.5 + Spring Data JPA + Spring Security + JWT + Validation |
| 数据库 | PostgreSQL 14+ (ddl-auto: update 自动建表) |
| 接口文档 | SpringDoc OpenAPI (Swagger UI) |

### 1.2 角色与职责

| 角色 | 账号 | 密码 | 职责 |
| --- | --- | --- | --- |
| 税务专员 | `tax01` | `123456` | 导入发票、标记风险原因 |
| 采购负责人 | `proc01` | `123456` | 补充采购合同、收货单等证明材料 |
| 财务经理 | `fin01` | `123456` | 确认最终处理结论（形成不可删除结论） |

### 1.3 核心业务规则

- ✅ **供应商黑名单联动**：供应商加入黑名单 → 其所有异常状态发票自动冻结报销，移出黑名单 → 自动解冻
- ✅ **缺少合同不能解除风险**：财务经理确认「解除风险」前必须确认已上传采购合同
- ✅ **不可删除的结论**：经理确认后，结论与确认信息自动锁定（`conclusion_deletable = false`），无法再修改
- ✅ **完整审计日志**：所有操作（标记风险、补充材料、加黑/移黑、确认结论）都有完整审批日志
- ✅ **JWT Token 鉴权**：前后端分离，基于 Token 的无状态认证

---

## 二、项目结构

```
.
├── backend/                              # 后端 Spring Boot 项目
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/invoice/risk/
│       │   ├── InvoiceRiskInvestigationApplication.java   # 启动类
│       │   ├── config/                     # 配置类 (Security/Cors/Web/JPA)
│       │   ├── context/                    # 用户上下文 ThreadLocal
│       │   ├── controller/                 # REST 接口层
│       │   ├── dto/                        # 请求响应 DTO
│       │   ├── entity/                     # JPA 实体类
│       │   ├── enums/                      # 业务枚举类
│       │   ├── exception/                  # 全局异常处理
│       │   ├── interceptor/                # JWT 鉴权拦截器
│       │   ├── repository/                 # JPA Repository
│       │   ├── service/                    # 业务服务层
│       │   └── util/                       # JWT 等工具类
│       └── resources/
│           ├── application.yml             # 应用配置
│           └── db/schema.sql               # 初始化 SQL（可选，默认 JPA 自动建表）
│
└── frontend/                             # 前端 React 项目
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json
    ├── index.html
    └── src/
        ├── main.tsx                       # 入口
        ├── App.tsx                        # 路由配置
        ├── api/                           # API 接口封装
        ├── components/                    # 通用组件
        ├── layouts/                       # 主布局
        ├── pages/                         # 页面
        │   ├── Login.tsx                  # 登录页
        │   ├── Dashboard.tsx              # 首页概览（统计卡片）
        │   ├── InvoiceList.tsx            # 发票管理（列表/导入/标记）
        │   ├── InvoiceDetail.tsx          # 发票详情（风险记录/材料/审批流）
        │   ├── SupplierList.tsx           # 供应商管理（黑名单联动）
        │   └── ApprovalLog.tsx            # 全局审批日志
        ├── store/AuthContext.tsx          # 登录态 Context
        ├── styles/global.css              # 全局样式
        ├── types/index.ts                 # TypeScript 类型定义
        └── utils/                         # request、枚举选项等工具
```

---

## 三、数据库表结构（自动建表）

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 系统用户表（税务/采购/财务 3 种角色） |
| `supplier` | 供应商档案表 |
| `supplier_blacklist` | 供应商黑名单历史（加黑/移黑全程留痕） |
| `invoice` | 发票主表，核心：`status` 状态机 + `reimbursement_frozen` 冻结标记 |
| `risk_record` | 风险记录表（1 张发票多条风险） |
| `risk_material` | 补充材料表（合同、收货单、订单等） |
| `approval_log` | 全量审批/操作日志（时间线展示） |

---

## 四、后端 API 接口一览

> 启动后访问 `http://localhost:8080/api/swagger-ui.html` 查看 Swagger 在线文档

### 4.1 认证
| Method | Path | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录获取 Token |

### 4.2 仪表盘
| Method | Path | 说明 |
| --- | --- | --- |
| GET | `/api/dashboard` | 获取首页统计数据 |

### 4.3 发票管理
| Method | Path | 说明 |
| --- | --- | --- |
| POST | `/api/invoices/import` | 导入单张发票 |
| POST | `/api/invoices/query` | 分页条件查询发票列表 |
| GET | `/api/invoices/{id}` | 获取发票详情 |

### 4.4 风险处理（按角色鉴权）
| Method | Path | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/risks/mark` | 税务专员 | 标记风险原因 |
| POST | `/api/risks/materials` | 采购负责人 | 补充合同/收货材料 |
| POST | `/api/risks/confirm` | 财务经理 | 确认结论（形成不可删除结论） |
| GET | `/api/risks/invoices/{invoiceId}/records` | - | 查询发票风险记录 |
| GET | `/api/risks/invoices/{invoiceId}/materials` | - | 查询发票补充材料 |

### 4.5 供应商管理
| Method | Path | 说明 |
| --- | --- | --- |
| POST | `/api/suppliers/import` | 导入供应商 |
| POST | `/api/suppliers/query` | 分页查询供应商 |
| GET | `/api/suppliers/all` | 查询全部供应商（下拉用） |
| GET | `/api/suppliers/{id}` | 获取供应商详情 |
| POST | `/api/suppliers/blacklist/add` | 加入黑名单（冻结其关联发票报销） |
| POST | `/api/suppliers/blacklist/remove` | 移出黑名单（解冻其关联发票报销） |

### 4.6 审批日志
| Method | Path | 说明 |
| --- | --- | --- |
| GET | `/api/approval-logs/invoices/{invoiceId}` | 查询单张发票的审批时间线 |
| GET | `/api/approval-logs/query` | 分页查询全局审批日志 |

---

## 五、快速启动

### 5.1 环境准备

1. **PostgreSQL 14+**，创建数据库：
   ```sql
   CREATE DATABASE invoice_risk_db WITH ENCODING = 'UTF8';
   ```
   默认连接配置（`backend/src/main/resources/application.yml`）：
   - 地址：`localhost:5432`
   - 数据库：`invoice_risk_db`
   - 用户名/密码：`postgres / postgres`

2. **JDK 17**（Spring Boot 3 要求）
3. **Maven 3.8+**
4. **Node.js 18+** + **npm/pnpm**

### 5.2 启动后端

```bash
cd backend

# 编译打包
mvn clean package -DskipTests

# 启动（默认端口 8080，context-path: /api）
mvn spring-boot:run
# 或：java -jar target/invoice-risk-investigation-1.0.0.jar
```

启动后自动建表 + 初始化 3 个默认账号，访问：
- Swagger UI: http://localhost:8080/api/swagger-ui.html

### 5.3 启动前端

```bash
cd frontend

# 安装依赖（国内建议使用 pnpm 或 cnpm 加速）
npm install

# 开发模式（默认端口 5173，已配置 /api 代理到 8080）
npm run dev

# 构建生产包
npm run build
npm run preview
```

浏览器访问 **http://localhost:5173** 进入系统。

---

## 六、推荐体验流程（End-to-End Demo）

1. 以 **税务专员 tax01 / 123456** 登录
   - 供应商管理 → 导入供应商或使用初始化的 3 条测试数据
   - 发票管理 → 导入发票（选择 SUP003「广州风险警示材料厂」会自动进入待审核+冻结报销，因为其在黑名单）
   - 发票详情 → 「标记风险」：选择「税号不符」+ 填写原因

2. 切换 **采购负责人 proc01 / 123456** 登录
   - 发票详情 → 「补充材料」：先上传「采购合同」必须，再上传「收货单」等
   - 确认左侧 Tab「补充材料」出现合同记录

3. 切换 **财务经理 fin01 / 123456** 登录
   - 发票详情 → 「确认处理结论」
     - 若缺少合同：弹窗直接红色提示 **禁止** 解除风险 ✅
     - 若材料齐全：可切换「解除风险 / 确认异常」
     - 填写处理结论 → 确认 → 结论自动标记 **不可删除**
   - 再次尝试标记风险 → 系统提示结论不可删除 ✅
   - 审批日志 Tab 可看到完整时间线

4. 任意角色 → 供应商管理
   - 选择 SUP001 → 「加入黑名单」 → 写原因
   - 进入其发票详情 → 报销已冻结 ✅ + 审批日志自动新增记录
   - 「移出黑名单」→ 报销自动解冻

---

## 七、发票状态机流转

```
NORMAL（正常）
   │
   ├─ 供应商加入黑名单 / 系统导入黑名单供应商 ──▶ PENDING_REVIEW（待审核）
   │
   └─ 税务专员「标记风险」 ──────────────────────▶ RISK_IDENTIFIED（已标记风险）
                                                       │
                                                       ▼
                                            MATERIALS_SUPPLEMENTED（材料已补充）
                                          采购负责人「补充合同等材料」
                                                       │
                                                       ▼
                                            财务经理「确认处理结论」
                                               │              │
                                               ▼              ▼
                                          RESOLVED       REJECTED（风险确认异常）
                                        （风险解除）      + reimbursement_frozen=true
                                        结论不可删除        结论不可删除
```

---

## 八、接口安全

- 登录接口开放，其他接口请求头必须携带：`Authorization: Bearer <token>`
- Token 由 JWT 签发，默认有效期 24 小时（`app.jwt.expiration=86400000` ms）
- 用户信息通过 `ThreadLocal`（`UserContext`）在一次请求内传递
- 业务层再次校验角色（即使前端隐藏按钮，后端也强制执行）

---

## 九、常见问题

| 问题 | 解决 |
| --- | --- |
| PostgreSQL 密码不对 | 修改 `application.yml` 中的 `spring.datasource.password` |
| 前端访问后端 CORS 报错 | 修改 `application.yml` 的 `app.cors.allowed-origins` 增加你的地址 |
| 登录报 401 | 后端服务未启动 / Token 过期，请重新登录 |
| 缺少合同无法解除风险 | 先使用采购负责人补充「材料类型=采购合同」的记录 |
| 结论确认后无法再次标记 | 结论不可删除是业务规则，是预期行为 |
