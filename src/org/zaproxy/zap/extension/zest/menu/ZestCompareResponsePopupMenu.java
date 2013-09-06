/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.zest.menu;

import java.awt.Component;
import java.lang.reflect.Method;

import org.mozilla.zest.core.v1.ZestRequest;
import org.mozilla.zest.core.v1.ZestResponse;
import org.mozilla.zest.core.v1.ZestStatement;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.zest.ExtensionZest;
import org.zaproxy.zap.extension.zest.ZestResultWrapper;
import org.zaproxy.zap.extension.zest.ZestResultsPanel;
import org.zaproxy.zap.extension.zest.ZestZapUtils;
import org.zaproxy.zap.view.PopupMenuHistoryReference;

public class ZestCompareResponsePopupMenu extends PopupMenuHistoryReference {

	private static final long serialVersionUID = 2282358266003940700L;

	private ExtensionZest extension;

	/**
	 * This method initializes 
	 * 
	 */
	public ZestCompareResponsePopupMenu(ExtensionZest extension) {
		super(Constant.messages.getString("zest.compare.popup"));
		this.extension = extension;
	}
	
	@Override
    public boolean isEnableForComponent(Component invoker) {
		// Call super method to set up 'lastInvoker' but ignore the result
		super.isEnableForComponent(invoker);
		if (Control.getSingleton().getExtensionLoader().getExtension("ExtensionDiff") == null) {
			// Diff extension has not been installed
			return false;
		}
		// TODO check to see if its an 'action' node
        return (extension.getLastRunScript() != null && 
        		invoker.getName() != null && 
        		invoker.getName().equals(ZestResultsPanel.TABLE_NAME));
    }

	@Override
    public boolean isSafe() {
    	return true;
    }

	@Override
	public void performAction(HistoryReference href) throws Exception {
    	ZestResultWrapper newRes = (ZestResultWrapper)href;
    	if (extension.getLastRunScript() != null && newRes != null && newRes.getScriptRequestIndex() >= 0) {
    		ZestStatement stmt = extension.getLastRunScript().getStatement(newRes.getScriptRequestIndex());
    		if (stmt instanceof ZestRequest) {
    			ZestRequest zr = (ZestRequest)stmt;
				ZestResponse resp = zr.getResponse();
				// Have tro use introspection as ExtensionDiff is an add-on and will therefore be
				// loaded using a different class loader
				Extension ext = Control.getSingleton().getExtensionLoader().getExtension("ExtensionDiff");
				if (ext != null) {
					Method method = ext.getClass().getMethod("showDiffDialog", new Class<?>[]{HttpMessage.class, HttpMessage.class});
					if (method != null) {
						method.invoke(ext, ZestZapUtils.toHttpMessage(zr, resp), newRes.getHttpMessage());
					}
				}
    		}
    	}
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		// Not used
		return false;
	}
}
