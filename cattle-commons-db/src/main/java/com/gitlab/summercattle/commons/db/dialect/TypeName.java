package com.gitlab.summercattle.commons.db.dialect;

class TypeName {

	private String simpleName;

	private String name;

	TypeName(String simpleName, String name) {
		this.simpleName = simpleName;
		this.name = name;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getName() {
		return name;
	}
}