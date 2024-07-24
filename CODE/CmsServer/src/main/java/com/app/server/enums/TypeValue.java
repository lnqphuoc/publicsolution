package com.app.server.enums;

public enum TypeValue {
	list_filed("listfield"),
	admin_key("admin_key"),
	table_admin("admin_table"),
	key_of_Table("key"),
	list_filter("listfilter"),
	query("query"),
	function("function"),
	page_limit("page_limit"),
	key_list("key_list"),
	queryOfFilter("query"),
	cities("city"),
	district("district"),
	orderStatus("order_status"),
	keyOfFilter("key"),
	export_excel_direct("export_excel_direct"),
	admin_filter("admin"),
	export_excel_path("export_excel_path");

	TypeValue(String name) {
		this.name = name;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
