-- 添加AI功能菜单
insert into sys_menu values('2000', 'AI功能', '1', '11', 'ai', 'system/ai', null, '', 1, 0, 'C', '0', '0', 'system:ai:list', 'robot', 'admin', sysdate(), '', null, 'AI功能菜单');

-- 添加AI功能按钮权限
insert into sys_menu values('2001', 'AI测试', '2000', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:ai:test', '#', 'admin', sysdate(), '', null, '');

-- 将AI功能菜单权限分配给管理员角色
insert into sys_role_menu values ('1', '2000');
insert into sys_role_menu values ('1', '2001');