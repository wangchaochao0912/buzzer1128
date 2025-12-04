# JSON数据写入服务API文档

## 概述

提供通用的JSON数据写入服务，支持将JSON格式的数据新增到指定文件中，可选择是否进行主键校验。

## 接口说明

### 新增数据接口

**URL:** `/api/data`

**方法:** `POST`

**请求头:** `Content-Type: application/json`

**请求体参数:**

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| fileName | String | 是 | 要写入的文件名 |
| jsonData | JSON Object | 是 | 要写入的JSON数据 |
| needValidation | Boolean | 否 | 是否需要主键校验，默认为false |
| primaryKey | String | 否 | 主键字段名，仅当needValidation为true时必填 |

### 请求示例

#### 无校验新增
```json
{
  "fileName": "users.json",
  "jsonData": {
    "id": 1,
    "name": "张三",
    "age": 25,
    "email": "zhangsan@example.com"
  },
  "needValidation": false
}
```

#### 带主键校验新增
```json
{
  "fileName": "users.json",
  "jsonData": {
    "id": 2,
    "name": "李四",
    "age": 30,
    "email": "lisi@example.com"
  },
  "needValidation": true,
  "primaryKey": "id"
}
```

### 响应说明

**成功响应:**
```json
"数据新增成功"
```

**错误响应:**

| HTTP状态码 | 说明 |
|------------|------|
| 400 | 参数错误或缺少必要参数 |
| 500 | 服务器内部错误 |

#### 错误响应示例

```json
"参数错误: 校验模式下主键字段名不能为空"
```

```json
"操作失败: 主键已存在: 1"
```

## 数据存储

- 数据将存储在项目根目录下的 `src/main/resources/` 目录中
- 每个文件对应一个JSON数组，每次新增操作会在数组末尾添加新元素
- 文件格式为JSON，带有缩进，便于查看和编辑

## 使用示例

### 使用curl命令测试

#### 无校验新增
```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "fileName": "users.json",
  "jsonData": {
    "id": 1,
    "name": "张三",
    "age": 25
  },
  "needValidation": false
}' http://localhost:8080/api/data
```

#### 带校验新增
```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "fileName": "users.json",
  "jsonData": {
    "id": 2,
    "name": "李四",
    "age": 30
  },
  "needValidation": true,
  "primaryKey": "id"
}' http://localhost:8080/api/data
```

## 注意事项

1. 文件名不能包含特殊字符，建议使用字母、数字和下划线
2. 主键字段名应与JSON数据中的字段名一致
3. 校验模式下，主键值必须唯一
4. 若文件不存在，系统会自动创建
5. 数据会追加到文件末尾，不会影响已有数据