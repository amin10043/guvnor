/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.guvnor.client.asseteditor.soa;

import org.drools.guvnor.client.asseteditor.AssetAttachmentFileWidget;
import org.drools.guvnor.client.asseteditor.RuleViewer;
import org.drools.guvnor.client.explorer.ClientFactory;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.rpc.Asset;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HTML;

/**
 * This widget deals with JAR file containing Java classes as a service
 * artifact.
 */
public class JarWidget extends AssetAttachmentFileWidget {

    public JarWidget(Asset asset,
                      RuleViewer viewer,
                      ClientFactory clientFactory,
                      EventBus eventBus) {
        super( asset,
               viewer,
               clientFactory,
               eventBus );
        super.addSupplementaryWidget( new HTML( ((Constants) GWT.create( Constants.class )).JarWidgetDescription() ) );
    }

    public ImageResource getIcon() {
        return images.modelLarge();
    }

    public String getOverallStyleName() {
        return "decision-Table-upload"; //NON-NLS
    }

}
