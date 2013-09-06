/**
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * @author Alessandro Secco: seccoale@gmail.com
 */
package org.zaproxy.zap.extension.zest.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.mozilla.zest.core.v1.ZestConditional;
import org.apache.log4j.Logger;
import org.mozilla.zest.core.v1.ZestContainer;
import org.mozilla.zest.core.v1.ZestElement;
import org.mozilla.zest.core.v1.ZestExpression;
import org.mozilla.zest.core.v1.ZestLoop;
import org.mozilla.zest.core.v1.ZestLoopFile;
import org.mozilla.zest.core.v1.ZestLoopInteger;
import org.mozilla.zest.core.v1.ZestLoopString;
import org.mozilla.zest.core.v1.ZestRequest;
import org.mozilla.zest.core.v1.ZestStatement;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.script.ScriptNode;
import org.zaproxy.zap.extension.zest.ExtensionZest;
import org.zaproxy.zap.extension.zest.ZestZapUtils;

public class ZestAddLoopPopupMenu extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = -8433923894855139684L;

	private ExtensionZest extension;
	private List<ExtensionPopupMenuItem> subMenus = new ArrayList<>();

	private static final Logger logger = Logger
			.getLogger(ZestAddConditionPopupMenu.class);

	/**
	 * This method initializes
	 * 
	 */
	public ZestAddLoopPopupMenu(ExtensionZest extension) {
		super("AddLoopX");
		this.extension = extension;
	}

	/**/
	@Override
	public String getParentMenuName() {
		return Constant.messages.getString("zest.loop.add.popup");
	}

	@Override
	public boolean isSubMenu() {
		return true;
	}

	@Override
	public boolean isDummyItem() {
		return true;
	}

	public boolean isEnableForComponent(Component invoker) {
		// Remove previous submenus
		for (ExtensionPopupMenuItem menu : subMenus) {
			View.getSingleton().getPopupMenu().removeMenu(menu);
		}
		subMenus.clear();
		if (extension.isScriptTree(invoker)) {
			ScriptNode node = extension.getSelectedZestNode();
			ZestElement ze = extension.getSelectedZestElement();

			if (node != null) {
				if (ze instanceof ZestRequest) {
					reCreateSubMenu(node.getParent(), node, (ZestRequest) ze);
					return true;
				} else if (ze instanceof ZestContainer /*
														 * && TODO !
														 * ZestTreeElement
														 * .isSubclass
														 * (node.getParent
														 * ().getZestElement(),
														 * ZestTreeElement
														 * .Type.PASSIVE_SCRIPT)
														 */) {
					if (ze instanceof ZestConditional
							&& ZestZapUtils.getShadowLevel(node) == 0) {
						return false;
					}
					reCreateSubMenu(node, null, null);
					return true;
					/*
					 * TODO } else if
					 * (ZestTreeElement.Type.COMMON_TESTS.equals(node
					 * .getTreeType())) { reCreateSubMenu(node, null, "BODY",
					 * ""); return true;
					 */
				} else if (ze instanceof ZestExpression) {
					return false;
				}
				if (node == null || node.isTemplate()) {
					return false;
				} else if (ze instanceof ZestRequest) {
					reCreateSubMenu(node.getParent(), node, (ZestRequest) ze);
					return true;
				} else if (ze instanceof ZestContainer) {
					reCreateSubMenu(node, null, null);
					return true;
				}
			}
		}
		return false;
	}

	private void reCreateSubMenu(ScriptNode parent, ScriptNode child,
			ZestStatement stmt) {
		List<ScriptNode> children = new LinkedList<>();
		children.add(child);
		createPopupAddActionMenu(parent, children, stmt, new ZestLoopString());
		try {
			createPopupAddActionMenu(parent, children, stmt, new ZestLoopFile());
		} catch (IOException e) {
			logger.debug(e.getMessage(), e);
		}
		createPopupAddActionMenu(parent, children, stmt, new ZestLoopInteger());
	}

	private void createPopupAddActionMenu(final ScriptNode parent,
			final List<ScriptNode> children, final ZestStatement stmt,
			final ZestLoop<?> za) {
		ZestPopupMenu menu = new ZestPopupMenu(
				Constant.messages.getString("zest.loop.add.popup"),
				ZestZapUtils.toUiString(za, false));
		menu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				extension.getDialogManager().showZestLoopDialog(parent,
						children, stmt, za, true, false);
			}
		});
		menu.setMenuIndex(this.getMenuIndex());
		View.getSingleton().getPopupMenu().addMenu(menu);
		this.subMenus.add(menu);
	}

	@Override
	public boolean isSafe() {
		return true;
	}

}
