collections=('sys_config' 'sys_dept' 'sys_dict_data' 'sys_dict_type' 'sys_djob_group' 'sys_djob_info' 'sys_djob_log' 'sys_djob_script' 'sys_djob_stats' 'sys_lock' 'sys_login_info' 'sys_menu' 'sys_notice' 'sys_op_log' 'sys_post' 'sys_role' 'sys_role_dept' 'sys_role_menu' 'sys_sequence' 'sys_user' 'sys_user_online' 'sys_user_post' 'sys_user_role' 'sys_runtime_config');
zs_array=${collections[@]};
for s_name in $zs_array;do
  echo exporting:$s_name
  ./mongoimport -h localhost:27017 -d training -c $s_name ~/mongo/$s_name.json
done

