-- =============================================
-- 企业发票风险协查系统 数据库初始化脚本
-- 数据库: PostgreSQL
-- 说明: 如果使用 JPA ddl-auto=update，此脚本仅用于初始化测试数据
-- =============================================

-- 创建数据库（请在 PostgreSQL 客户端中手动执行）
-- CREATE DATABASE invoice_risk_db WITH ENCODING = 'UTF8';

-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 2. 供应商表
-- =============================================
CREATE TABLE IF NOT EXISTS supplier (
    id BIGSERIAL PRIMARY KEY,
    supplier_code VARCHAR(100) NOT NULL UNIQUE,
    supplier_name VARCHAR(200) NOT NULL,
    tax_number VARCHAR(50) NOT NULL,
    address VARCHAR(200),
    contact_phone VARCHAR(30),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    credit_limit DECIMAL(18, 2),
    is_blacklisted BOOLEAN NOT NULL DEFAULT FALSE,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 3. 供应商黑名单表
-- =============================================
CREATE TABLE IF NOT EXISTS supplier_blacklist (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    supplier_code VARCHAR(100),
    supplier_name VARCHAR(200),
    reason VARCHAR(500),
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    removed_at TIMESTAMP,
    removed_by BIGINT,
    remove_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_blacklist_supplier ON supplier_blacklist(supplier_id, is_active);

-- =============================================
-- 4. 发票表
-- =============================================
CREATE TABLE IF NOT EXISTS invoice (
    id BIGSERIAL PRIMARY KEY,
    invoice_code VARCHAR(50) NOT NULL UNIQUE,
    invoice_number VARCHAR(20) NOT NULL,
    invoice_type VARCHAR(20) NOT NULL,
    invoice_date DATE NOT NULL,
    amount_before_tax DECIMAL(18, 2) NOT NULL,
    tax_amount DECIMAL(18, 2) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    tax_rate DECIMAL(5, 4),
    supplier_id BIGINT NOT NULL,
    supplier_code VARCHAR(100),
    supplier_name VARCHAR(200),
    buyer_tax_number VARCHAR(50),
    buyer_name VARCHAR(200),
    goods_description VARCHAR(500),
    drawer VARCHAR(200),
    payee VARCHAR(200),
    checker VARCHAR(200),
    remark VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    reimbursement_frozen BOOLEAN NOT NULL DEFAULT FALSE,
    conclusion VARCHAR(500),
    confirmed_by BIGINT,
    confirmed_by_name VARCHAR(50),
    confirmed_at TIMESTAMP,
    conclusion_deletable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_invoice_code ON invoice(invoice_code);
CREATE INDEX IF NOT EXISTS idx_invoice_status ON invoice(status);
CREATE INDEX IF NOT EXISTS idx_invoice_supplier ON invoice(supplier_id);
CREATE INDEX IF NOT EXISTS idx_invoice_date ON invoice(invoice_date);

-- =============================================
-- 5. 风险记录表
-- =============================================
CREATE TABLE IF NOT EXISTS risk_record (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    invoice_code VARCHAR(50),
    risk_type VARCHAR(50) NOT NULL,
    risk_description VARCHAR(1000),
    mark_reason VARCHAR(500),
    marked_by BIGINT NOT NULL,
    marked_by_name VARCHAR(50),
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolve_description VARCHAR(1000),
    resolved_by BIGINT,
    resolved_by_name VARCHAR(50),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_risk_invoice ON risk_record(invoice_id);

-- =============================================
-- 6. 风险材料表
-- =============================================
CREATE TABLE IF NOT EXISTS risk_material (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    invoice_code VARCHAR(50),
    material_type VARCHAR(50) NOT NULL,
    material_name VARCHAR(200),
    material_url VARCHAR(500),
    contract_number VARCHAR(50),
    contract_date DATE,
    delivery_note_number VARCHAR(50),
    delivery_date DATE,
    remark VARCHAR(500),
    uploaded_by BIGINT NOT NULL,
    uploaded_by_name VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_material_invoice ON risk_material(invoice_id);

-- =============================================
-- 7. 审批日志表
-- =============================================
CREATE TABLE IF NOT EXISTS approval_log (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    invoice_code VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    remark VARCHAR(1000),
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(50),
    operator_role VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_approval_invoice ON approval_log(invoice_id);
CREATE INDEX IF NOT EXISTS idx_approval_created ON approval_log(created_at);

-- =============================================
-- 初始化测试数据 - 供应商
-- =============================================
INSERT INTO supplier (supplier_code, supplier_name, tax_number, address, contact_phone, bank_name, bank_account, credit_limit, is_blacklisted, remark)
SELECT 'SUP001', '北京阳光科技有限公司', '91110108MA001ABC12', '北京市海淀区中关村大街1号', '010-88888888', '工商银行北京分行', '0200001009000000001', 500000.00, FALSE, '长期优质供应商'
WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE supplier_code = 'SUP001');

INSERT INTO supplier (supplier_code, supplier_name, tax_number, address, contact_phone, bank_name, bank_account, credit_limit, is_blacklisted, remark)
SELECT 'SUP002', '上海诚信贸易有限公司', '91310101MA002DEF34', '上海市浦东新区陆家嘴环路1000号', '021-66666666', '建设银行上海分行', '0300002009000000002', 300000.00, FALSE, '一般供应商'
WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE supplier_code = 'SUP002');

INSERT INTO supplier (supplier_code, supplier_name, tax_number, address, contact_phone, bank_name, bank_account, credit_limit, is_blacklisted, remark)
SELECT 'SUP003', '广州风险警示材料厂', '91440101MA003GHI56', '广州市天河区天河路385号', '020-77777777', '农业银行广州分行', '0400003009000000003', 100000.00, TRUE, '历史存在发票违规记录'
WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE supplier_code = 'SUP003');
