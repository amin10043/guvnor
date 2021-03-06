/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.client.decisiontable.widget;

import org.drools.guvnor.client.util.GWTDateConverter;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A Vertical Decision Table composed of a VerticalDecoratedGridWidget
 */
public class VerticalDecisionTableWidget extends AbstractDecisionTableWidget {

    public VerticalDecisionTableWidget(DecisionTableControlsWidget ctrls,
                                       SuggestionCompletionEngine sce,
                                       boolean isReadOnly,
                                       EventBus eventBus) {
        super( ctrls,
               sce,
               isReadOnly,
               eventBus );

        VerticalPanel vp = new VerticalPanel();

        //Factories for new cell elements
        this.cellFactory = new DecisionTableCellFactory( sce,
                                                         isReadOnly,
                                                         eventBus );
        this.cellValueFactory = new DecisionTableCellValueFactory( sce );

        //Date converter is injected so a GWT compatible one can be used here and another in testing
        DecisionTableCellValueFactory.injectDateConvertor( GWTDateConverter.getInstance() );

        // Construct the widget from which we're composed
        widget = new VerticalDecoratedDecisionTableGridWidget( resources,
                                                               cellFactory,
                                                               cellValueFactory,
                                                               isReadOnly,
                                                               eventBus );

        vp.add( widget );
        vp.add( ctrls );
        initWidget( vp );
    }

}
