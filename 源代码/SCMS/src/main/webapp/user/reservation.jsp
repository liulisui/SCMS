<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.scms.model.Campus" %>
<%@ page import="org.example.scms.model.Department" %>
<%@ page import="org.example.scms.model.User" %>
<%
    String reservationType = (String) request.getParameter("type");
    if (reservationType == null) {
        reservationType = "public";
    }
    
    // 获取校区和部门数据
    List<Campus> campuses = (List<Campus>) request.getAttribute("campuses");
    List<Department> departments = (List<Department>) request.getAttribute("departments");
%>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>新建预约申请 - 校园通行码预约管理系统</title>
                                        <style>
                                            * {
                                                margin: 0;
                                                padding: 0;
                                                box-sizing: border-box;
                                            }

                                            body {
                                                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                                                background: #f8fafc;
                                                color: #333;
                                            }

                                            .header {
                                                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                                color: white;
                                                padding: 1rem 0;
                                                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                                            }

                                            .header-content {
                                                max-width: 1200px;
                                                margin: 0 auto;
                                                padding: 0 2rem;
                                                display: flex;
                                                justify-content: space-between;
                                                align-items: center;
                                            }

                                            .back-btn {
                                                background: rgba(255, 255, 255, 0.2);
                                                color: white;
                                                border: none;
                                                padding: 0.5rem 1rem;
                                                border-radius: 5px;
                                                text-decoration: none;
                                                transition: background 0.3s;
                                            }

                                            .back-btn:hover {
                                                background: rgba(255, 255, 255, 0.3);
                                            }

                                            .container {
                                                max-width: 800px;
                                                margin: 2rem auto;
                                                padding: 0 2rem;
                                            }

                                            .form-card {
                                                background: white;
                                                border-radius: 12px;
                                                padding: 2rem;
                                                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
                                                border: 1px solid #e2e8f0;
                                            }

                                            .form-header {
                                                text-align: center;
                                                margin-bottom: 2rem;
                                            }

                                            .form-header h1 {
                                                color: #1a202c;
                                                font-size: 1.75rem;
                                                margin-bottom: 0.5rem;
                                            }

                                            .form-header p {
                                                color: #64748b;
                                            }

                                            .reservation-type-selector {
                                                display: grid;
                                                grid-template-columns: 1fr 1fr;
                                                gap: 1rem;
                                                margin-bottom: 2rem;
                                            }

                                            .type-card {
                                                padding: 1.5rem;
                                                border: 2px solid #e5e7eb;
                                                border-radius: 12px;
                                                cursor: pointer;
                                                transition: all 0.3s ease;
                                                text-align: center;
                                            }

                                            .type-card:hover {
                                                border-color: #667eea;
                                                box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
                                            }

                                            .type-card.selected {
                                                border-color: #667eea;
                                                background: rgba(102, 126, 234, 0.05);
                                            }

                                            .type-card .icon {
                                                font-size: 2.5rem;
                                                margin-bottom: 0.5rem;
                                            }

                                            .type-card h3 {
                                                color: #1a202c;
                                                margin-bottom: 0.5rem;
                                            }

                                            .type-card p {
                                                color: #64748b;
                                                font-size: 0.9rem;
                                            }

                                            .form-section {
                                                margin-bottom: 2rem;
                                            }

                                            .section-title {
                                                color: #374151;
                                                font-size: 1.125rem;
                                                font-weight: 600;
                                                margin-bottom: 1rem;
                                                padding-bottom: 0.5rem;
                                                border-bottom: 2px solid #e5e7eb;
                                            }

                                            .form-group {
                                                margin-bottom: 1.5rem;
                                            }

                                            .form-row {
                                                display: grid;
                                                grid-template-columns: 1fr 1fr;
                                                gap: 1rem;
                                            }

                                            .form-group label {
                                                display: block;
                                                color: #374151;
                                                font-weight: 500;
                                                margin-bottom: 0.5rem;
                                            }

                                            .required {
                                                color: #ef4444;
                                            }

                                            .form-group input,
                                            .form-group select,
                                            .form-group textarea {
                                                width: 100%;
                                                padding: 12px 16px;
                                                border: 2px solid #e5e7eb;
                                                border-radius: 8px;
                                                font-size: 1rem;
                                                transition: all 0.3s ease;
                                                background: #fff;
                                            }

                                            .form-group input:focus,
                                            .form-group select:focus,
                                            .form-group textarea:focus {
                                                outline: none;
                                                border-color: #667eea;
                                                box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
                                            }

                                            .form-group textarea {
                                                resize: vertical;
                                                min-height: 100px;
                                            }

                                            .official-fields {
                                                display: none;
                                                background: rgba(245, 158, 11, 0.05);
                                                padding: 1.5rem;
                                                border-radius: 8px;
                                                border: 1px solid rgba(245, 158, 11, 0.2);
                                                margin-bottom: 1rem;
                                            }

                                            .official-fields.show {
                                                display: block;
                                            }

                                            .submit-btn {
                                                width: 100%;
                                                padding: 1rem;
                                                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                                color: white;
                                                border: none;
                                                border-radius: 8px;
                                                font-size: 1.1rem;
                                                font-weight: 600;
                                                cursor: pointer;
                                                transition: all 0.3s ease;
                                            }

                                            .submit-btn:hover {
                                                transform: translateY(-2px);
                                                box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
                                            }

                                            .submit-btn:disabled {
                                                background: #9ca3af;
                                                cursor: not-allowed;
                                                transform: none;
                                                box-shadow: none;
                                            }

                                            .alert {
                                                padding: 12px;
                                                border-radius: 8px;
                                                margin-bottom: 1rem;
                                                font-size: 0.9rem;
                                            }

                                            .alert-error {
                                                background: #fee;
                                                color: #c33;
                                                border: 1px solid #fcc;
                                            }

                                            .alert-success {
                                                background: #efe;
                                                color: #363;
                                                border: 1px solid #cfc;
                                            }

                                            .help-text {
                                                font-size: 0.875rem;
                                                color: #6b7280;
                                                margin-top: 0.25rem;
                                            }

                                            @media (max-width: 768px) {
                                                .reservation-type-selector {
                                                    grid-template-columns: 1fr;
                                                }

                                                .form-row {
                                                    grid-template-columns: 1fr;
                                                }

                                                .container {
                                                    padding: 0 1rem;
                                                }

                                                .form-card {
                                                    padding: 1.5rem;
                                                }

                                                .header-content {
                                                    padding: 0 1rem;
                                                }                                            }
                                        </style>
                                    </head>

                                    <body>                                        <header class="header">
                                            <div class="header-content">
                                                <h1>新建预约申请</h1>
                                                <a href="${pageContext.request.contextPath}/index.jsp"
                                                    class="back-btn">返回首页</a>
                                            </div>
                                        </header>

                                        <div class="container">
                                            <div class="form-card">
                                                <div class="form-header">
                                                    <h1>校园通行码预约申请</h1>
                                                    <p>请选择预约类型并填写完整信息，我们将在24小时内完成审核</p>
                                                </div>

                                                <% if (request.getAttribute("error") !=null) { %>
                                                    <div class="alert alert-error">
                                                        <%= request.getAttribute("error") %>
                                                    </div>
                                                    <% } %>

                                                        <% if (request.getAttribute("success") !=null) { %>
                                                            <div class="alert alert-success">
                                                                <%= request.getAttribute("success") %>
                                                            </div>                                                            <% } %>
                                                <!-- 预约类型选择器 -->
                                                <div class="reservation-type-selector">
                                                    <div class="type-card <%= "public".equals(reservationType) ? "selected" : "" %>" onclick="selectType('public')">
                                                        <div class="icon">👥</div>
                                                        <h3>社会公众预约</h3>
                                                        <p>适用于个人或团体校园参观，文化体验等一般性访问</p>
                                                    </div>
                                                    <div class="type-card <%= "official".equals(reservationType) ? "selected" : "" %>" onclick="selectType('official')">
                                                        <div class="icon">💼</div>
                                                        <h3>公务来访</h3>
                                                        <p>适用于公务活动、学术交流、商务合作等正式访问</p>
                                                    </div>
                                                </div>                                                                <form method="post"
                                                                    action="${pageContext.request.contextPath}/user/reservation"
                                                                    id="reservationForm">
                                                                    <input type="hidden" name="reservationType"
                                                                        id="reservationTypeInput"
                                                                        value="<%= reservationType %>">

                                                                    <div class="form-section">
                                                                        <div class="section-title">基本信息</div>

                                                                        <div class="form-group">
                                                                            <label for="realName">申请人姓名 <span
                                                                                    class="required">*</span></label>                                                                            <input type="text" id="realName"
                                                                                name="visitorName" placeholder="请输入真实姓名"
                                                                                required>
                                                                        </div>

                                                                        <div class="form-row">
                                                                            <div class="form-group">
                                                                                <label for="realIdCard">身份证号 <span
                                                                                        class="required">*</span></label>                                                                                <input type="text" id="realIdCard"
                                                                                    name="visitorIdCard"
                                                                                    placeholder="请输入18位身份证号"
                                                                                    maxlength="18" required>
                                                                            </div>                                                                            <div class="form-group">
                                                                                <label for="realPhone">联系电话 <span
                                                                                        class="required">*</span></label>                                                                                <input type="tel" id="realPhone"
                                                                                    name="visitorPhone"
                                                                                    pattern="^1[3-9]\d{9}$"
                                                                                    maxlength="11"
                                                                                    placeholder="请输入11位手机号，如：13800138000"
                                                                                    title="请输入正确的11位手机号码，以1开头，第二位为3-9"
                                                                                    required>
                                                                                <div class="help-text">请输入正确的11位手机号码</div>
                                                                            </div>
                                                                        </div>

                                                                        <div class="form-row">
                                                                            <div class="form-group">
                                                                                <label for="campus">预约校区 <span
                                                                                        class="required">*</span></label>
                                                <select id="campus" name="campusId" required>
                                    <option value="">请选择校区</option>
                                    <% if (campuses != null) { 
                                        for (Campus campus : campuses) { %>
                                        <option value="<%= campus.getId() %>">
                                            <%= campus.getCampusName() %>
                                        </option>
                                    <% } 
                                    } else { %>
                                                                                            <option value="1">屏峰校区
                                                                                            </option>
                                                                                            <option value="2">朝晖校区
                                                                                            </option>
                                                                                            <option value="3">莫干山校区
                                                                                            </option>
                                                                                            <% } %>
                                                                                </select>
                                                                            </div>
                                                                            <div class="form-group">
                                                                                <label for="organization">所在单位 <span
                                                                                        class="required">*</span></label>
                                                                                <input type="text" id="organization"
                                                                                    name="organization"
                                                                                    placeholder="请输入您所在的工作单位或学校"
                                                                                    required>
                                                                            </div>
                                                                        </div>                                                                        <div class="form-row">
                                                                            <div class="form-group">
                                                                                <label for="transportMode">交通方式</label>
                                                                                <select id="transportMode"
                                                                                    name="transportMode">
                                                                                    <option value="">请选择交通方式</option>
                                                                                    <option value="步行">步行</option>
                                                                                    <option value="自行车">自行车</option>
                                                                                    <option value="电动车">电动车</option>
                                                                                    <option value="私家车">私家车</option>
                                                                                    <option value="出租车">出租车</option>
                                                                                    <option value="公交车">公交车</option>
                                                                                    <option value="地铁">地铁</option>
                                                                                </select>
                                                                            </div>
                                                                            <div class="form-group">
                                                                                <label for="licensePlate">车牌号</label>                                                                                <input type="text" id="licensePlate"
                                                                                    name="vehicleNumber"
                                                                                    placeholder="如有车辆请填写，格式如：浙A12345">
                                                                                <div class="help-text">驾驶机动车入校时必填</div>
                                                                            </div>
                                                                        </div>

                                                                        <div class="form-group">
                                                                            <label for="companions">随行人员信息</label>
                                                                            <textarea id="companions" name="companions"
                                                                                placeholder="如有随行人员，请填写：姓名、身份证号、手机号，多人用分号分隔&#13;&#10;例如：张三,110101199001011234,13800138000;李四,110101199002022345,13800138001"></textarea>
                                                                            <div class="help-text">
                                                                                格式：姓名,身份证号,手机号;姓名,身份证号,手机号（可留空）</div>
                                                                        </div>
                                                                    </div>

                                                                    <!-- 公务预约专用字段 -->
                                                                    <div class="official-fields" id="officialFields">
                                                                        <div class="section-title">公务访问信息</div>

                                                                        <div class="form-row">
                                                                            <div class="form-group">
                                                                                <label for="officialDepartment">访问部门
                                                                                    <span
                                                                                        class="required">*</span></label>                                                                                <select id="officialDepartment"
                                                                                    name="hostDepartmentId">
                                                                                    <option value="">请选择访问部门</option>
                                                                                    <% if (departments !=null) { for
                                                                                        (Department dept : departments)
                                                                                        { %>
                                                                                        <option
                                                                                            value="<%= dept.getId() %>">
                                                                                            <%= dept.getName() %>
                                                                                        </option>
                                                                                        <% } } else { %>
                                                                                            <option value="1">校长办公室
                                                                                            </option>
                                                                                            <option value="2">教务处
                                                                                            </option>
                                                                                            <option value="3">学生处
                                                                                            </option>
                                                                                            <option value="4">人事处
                                                                                            </option>
                                                                                            <option value="5">财务处
                                                                                            </option>
                                                                                            <option value="6">科研处
                                                                                            </option>
                                                                                            <option value="7">后勤处
                                                                                            </option>
                                                                                            <option value="8">保卫处
                                                                                            </option>
                                                                                            <option value="9">计算机学院
                                                                                            </option>
                                                                                            <option value="10">电子信息学院
                                                                                            </option>
                                                                                            <% } %>
                                                                                </select>
                                                                            </div>
                                                                            <div class="form-group">
                                                                                <label for="officialContactPerson">接待人
                                                                                    <span
                                                                                        class="required">*</span></label>                                                                                <input type="text"
                                                                                    id="officialContactPerson"
                                                                                    name="contactPerson"
                                                                                    placeholder="请输入接待人姓名">
                                                                            </div>
                                                                        </div>                                                                        <div class="form-group">
                                                                            <label for="officialContactPhone">接待人电话
                                                                                <span class="required">*</span></label>                                                                            <input type="tel" id="officialContactPhone"
                                                                                name="contactPhone"
                                                                                pattern="^1[3-9]\d{9}$"
                                                                                maxlength="11"
                                                                                placeholder="请输入11位手机号，如：13800138000"
                                                                                title="请输入正确的11位手机号码，以1开头，第二位为3-9">
                                                                            <div class="help-text">请输入正确的11位手机号码</div>                                                                        </div>
                                                                    </div><div class="form-section">
                                                                        <div class="section-title">预约信息</div>                                                                        <div class="form-group">
                                                                            <label for="purpose">来访目的 <span
                                                                                    class="required">*</span></label>
                                                                            <textarea id="purpose" name="visitReason"
                                                                                placeholder="请详细说明您的来访目的和具体目的地，如：教学楼A座参加学术会议..."
                                                                                required></textarea>
                                                                            <div class="help-text">请如实填写来访目的和具体目的地，便于我们进行审核
                                                                            </div>
                                                                        </div>

                                                                        <div class="form-row">
                                                                            <div class="form-group">
                                                                                <label for="visitDate">访问日期 <span
                                                                                        class="required">*</span></label>
                                                                                <input type="date"
                                                                                    id="visitDate" name="visitDate"
                                                                                    required>
                                                                                <div class="help-text">请选择您计划的访问日期</div>
                                                                            </div>
                                                                            <div class="form-group">
                                                                                <label for="visitTimeStart">开始时间 <span
                                                                                        class="required">*</span></label>
                                                                                <input type="time"
                                                                                    id="visitTimeStart" name="visitTimeStart"
                                                                                    required>
                                                                                <div class="help-text">请选择到达时间</div>
                                                                            </div>
                                                                        </div>
                                                                        
                                                                        <div class="form-row">
                                                                            <div class="form-group">
                                                                                <label for="visitTimeEnd">结束时间 <span
                                                                                        class="required">*</span></label>
                                                                                <input type="time"
                                                                                    id="visitTimeEnd" name="visitTimeEnd"
                                                                                    required>
                                                                                <div class="help-text">请选择离开时间</div>
                                                                            </div>
                                                                            <div class="form-group">
                                                                                <label for="accompanyingPersons">随行人数</label>
                                                                                <input type="number"
                                                                                    id="accompanyingPersons" name="accompanyingPersons"
                                                                                    min="0" max="20" value="0"
                                                                                    placeholder="不包括申请人本人">
                                                                                <div class="help-text">不包括申请人本人</div>
                                                                            </div>
                                                                        </div>
                                                                    </div>

                                                                    <button type="submit" class="submit-btn"
                                                                        id="submitBtn">提交预约申请</button>
                                                                </form>
                                            </div>
                                        </div>

                                        <script>
                                            // 选择预约类型
                                            function selectType(type) {
                                                document.getElementById('reservationTypeInput').value = type;

                                                // 更新UI
                                                document.querySelectorAll('.type-card').forEach(card => {
                                                    card.classList.remove('selected');
                                                });

                                                if (type === 'public') {
                                                    document.querySelectorAll('.type-card')[0].classList.add('selected');
                                                    document.getElementById('officialFields').classList.remove('show');
                                                    setOfficialFieldsRequired(false);
                                                } else {
                                                    document.querySelectorAll('.type-card')[1].classList.add('selected');
                                                    document.getElementById('officialFields').classList.add('show');
                                                    setOfficialFieldsRequired(true);
                                                }
                                            }                                            // 设置公务字段的必填状态
                                            function setOfficialFieldsRequired(required) {
                                                const officialDepartment = document.getElementById('officialDepartment');
                                                const officialContactPerson = document.getElementById('officialContactPerson');
                                                const officialContactPhone = document.getElementById('officialContactPhone');

                                                if (officialDepartment) officialDepartment.required = required;
                                                if (officialContactPerson) officialContactPerson.required = required;
                                                if (officialContactPhone) {
                                                    officialContactPhone.required = required;
                                                    // 如果是公务预约，添加接待人手机号验证
                                                    if (required) {
                                                        setupPhoneValidation('officialContactPhone');
                                                    }
                                                }
                                            }

                                            // 设置手机号验证
                                            function setupPhoneValidation(inputId) {
                                                const input = document.getElementById(inputId);
                                                if (!input) return;
                                                
                                                // 移除已有的事件监听器
                                                input.removeEventListener('input', phoneValidationHandler);
                                                // 添加新的事件监听器
                                                input.addEventListener('input', phoneValidationHandler);
                                            }

                                            // 手机号验证处理函数
                                            function phoneValidationHandler() {
                                                const phone = this.value;
                                                const pattern = /^1[3-9]\d{9}$/;
                                                
                                                // 只允许输入数字
                                                this.value = phone.replace(/[^\d]/g, '');
                                                
                                                if (this.value.length === 11) {
                                                    if (!pattern.test(this.value)) {
                                                        this.setCustomValidity('请输入正确的11位手机号码，第一位为1，第二位为3-9');
                                                        if (this.id === 'officialContactPhone') {
                                                            showAlert('error', '接待人手机号格式不正确，请检查输入');
                                                        } else {
                                                            showAlert('error', '手机号格式不正确，请检查输入');
                                                        }
                                                    } else {
                                                        this.setCustomValidity('');
                                                    }
                                                } else if (this.value.length > 0 && this.value.length < 11) {
                                                    this.setCustomValidity('手机号码必须是11位数字');
                                                } else {
                                                    this.setCustomValidity('');
                                                }
                                            }// 页面加载时初始化
                                            document.addEventListener('DOMContentLoaded', function () {
                                                // 设置最小日期为当前日期
                                                const now = new Date();
                                                const year = now.getFullYear();
                                                const month = String(now.getMonth() + 1).padStart(2, '0');
                                                const day = String(now.getDate()).padStart(2, '0');

                                                const minDate = `${year}-${month}-${day}`;
                                                document.getElementById('visitDate').min = minDate;

                                                // 初始化预约类型
                                                const currentType = document.getElementById('reservationTypeInput').value;
                                                selectType(currentType);

                                                // 访问日期变化时验证时间
                                                document.getElementById('visitDate').addEventListener('change', function () {
                                                    validateDateTime();
                                                });

                                                document.getElementById('visitTimeStart').addEventListener('change', function () {
                                                    validateDateTime();
                                                });

                                                document.getElementById('visitTimeEnd').addEventListener('change', function () {
                                                    validateDateTime();
                                                });

                                                // 验证访问时间
                                                function validateDateTime() {
                                                    const visitDate = document.getElementById('visitDate').value;
                                                    const visitTimeStart = document.getElementById('visitTimeStart').value;
                                                    const visitTimeEnd = document.getElementById('visitTimeEnd').value;

                                                    if (visitDate && visitTimeStart) {
                                                        const visitDateTime = new Date(visitDate + 'T' + visitTimeStart);
                                                        const currentDateTime = new Date();

                                                        if (visitDateTime <= currentDateTime) {
                                                            document.getElementById('visitDate').setCustomValidity('访问时间不能早于当前时间');
                                                            document.getElementById('visitTimeStart').setCustomValidity('访问时间不能早于当前时间');
                                                            showAlert('error', '访问时间不能早于当前时间，请重新选择！');
                                                        } else {
                                                            document.getElementById('visitDate').setCustomValidity('');
                                                            document.getElementById('visitTimeStart').setCustomValidity('');
                                                        }
                                                    }

                                                    if (visitTimeStart && visitTimeEnd) {
                                                        if (visitTimeStart >= visitTimeEnd) {
                                                            document.getElementById('visitTimeEnd').setCustomValidity('结束时间必须晚于开始时间');
                                                            showAlert('error', '结束时间必须晚于开始时间！');
                                                        } else {
                                                            document.getElementById('visitTimeEnd').setCustomValidity('');
                                                        }
                                                    }
                                                }

                                                // 显示提示信息
                                                function showAlert(type, message) {
                                                    // 移除已存在的提示
                                                    const existingAlert = document.querySelector('.alert-temp');
                                                    if (existingAlert) {
                                                        existingAlert.remove();
                                                    }

                                                    // 创建新提示
                                                    const alert = document.createElement('div');
                                                    alert.className = `alert alert-${type} alert-temp`;
                                                    alert.textContent = message;
                                                    
                                                    const formCard = document.querySelector('.form-card');
                                                    const formHeader = document.querySelector('.form-header');
                                                    formCard.insertBefore(alert, formHeader.nextSibling);

                                                    // 3秒后自动移除
                                                    setTimeout(() => {
                                                        if (alert.parentNode) {
                                                            alert.remove();
                                                        }
                                                    }, 3000);
                                                }                                                // 交通方式变化时控制车牌号是否必填
                                                document.getElementById('transportMode').addEventListener('change', function () {
                                                    const licensePlate = document.getElementById('licensePlate');
                                                    if (this.value === '私家车') {
                                                        licensePlate.required = true;
                                                        licensePlate.placeholder = '私家车必须填写车牌号，格式如：浙A12345';
                                                    } else {
                                                        licensePlate.required = false;
                                                        licensePlate.placeholder = '如有车辆请填写，格式如：浙A12345';
                                                    }
                                                });

                                                // 身份证号码格式验证
                                                document.getElementById('realIdCard').addEventListener('input', function () {
                                                    const idCard = this.value;
                                                    if (idCard.length === 18) {
                                                        const pattern = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
                                                        if (!pattern.test(idCard)) {
                                                            this.setCustomValidity('请输入正确的18位身份证号码');
                                                        } else {
                                                            this.setCustomValidity('');
                                                        }
                                                    }
                                                });                                                // 手机号码格式验证 - 访客
                                                setupPhoneValidation('realPhone');// 表单提交前验证
                                                document.getElementById('reservationForm').addEventListener('submit', function (e) {
                                                    const visitDate = document.getElementById('visitDate').value;
                                                    const visitTimeStart = document.getElementById('visitTimeStart').value;
                                                    const visitTimeEnd = document.getElementById('visitTimeEnd').value;
                                                    const reservationType = document.getElementById('reservationTypeInput').value;

                                                    // 验证访问时间
                                                    if (visitDate && visitTimeStart) {
                                                        const visitDateTime = new Date(visitDate + 'T' + visitTimeStart);
                                                        const currentDateTime = new Date();

                                                        if (visitDateTime <= currentDateTime) {
                                                            e.preventDefault();
                                                            showAlert('error', '访问时间不能早于当前时间，请重新选择！');
                                                            return false;
                                                        }
                                                    }

                                                    if (visitTimeStart && visitTimeEnd && visitTimeStart >= visitTimeEnd) {
                                                        e.preventDefault();
                                                        showAlert('error', '结束时间必须晚于开始时间！');
                                                        return false;
                                                    }

                                                    // 验证访客手机号
                                                    const visitorPhone = document.getElementById('realPhone').value;
                                                    const phonePattern = /^1[3-9]\d{9}$/;
                                                    if (!phonePattern.test(visitorPhone)) {
                                                        e.preventDefault();
                                                        showAlert('error', '访客手机号格式不正确，请输入正确的11位手机号！');
                                                        document.getElementById('realPhone').focus();
                                                        return false;
                                                    }

                                                    // 如果是公务预约，验证接待人手机号
                                                    if (reservationType === 'official') {
                                                        const contactPhone = document.getElementById('officialContactPhone').value;
                                                        if (!phonePattern.test(contactPhone)) {
                                                            e.preventDefault();
                                                            showAlert('error', '接待人手机号格式不正确，请输入正确的11位手机号！');
                                                            document.getElementById('officialContactPhone').focus();
                                                            return false;
                                                        }
                                                    }

                                                    const submitBtn = document.getElementById('submitBtn');
                                                    submitBtn.disabled = true;
                                                    submitBtn.textContent = '提交中...';

                                                    // 如果验证失败，重新启用按钮
                                                    setTimeout(() => {
                                                        if (!this.checkValidity()) {
                                                            submitBtn.disabled = false;
                                                            submitBtn.textContent = '提交预约申请';
                                                        }
                                                    }, 100);
                                                });
                                            });                                    </script>
                                    </body>

                                    </html>