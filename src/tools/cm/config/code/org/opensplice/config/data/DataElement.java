/*
 *                         OpenSplice DDS
 *
 *   This software and documentation are Copyright 2006 to 2013 PrismTech
 *   Limited and its licensees. All rights reserved. See file:
 *
 *                     $OSPL_HOME/LICENSE 
 *
 *   for full copyright notice and license terms. 
 *
 */
package org.opensplice.config.data;

import java.util.ArrayList;

import org.opensplice.common.util.ConfigModeIntializer;
import org.opensplice.config.meta.MetaAttribute;
import org.opensplice.config.meta.MetaElement;
import org.opensplice.config.meta.MetaNode;
import org.opensplice.config.meta.MetaValue;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class DataElement extends DataNode {
    private ArrayList<DataNode> children = null;
    
    public DataElement(MetaElement metadata, Element node) throws DataException {
        super(metadata, node);
        
        if(!node.getNodeName().equals(metadata.getName())){
            throw new DataException("Metadata and data do not match.");
        }
        this.children = new ArrayList<DataNode>();
    }
    
    protected DataNode addChild(DataNode node, int addToDOM, String value) throws DataException {
        /*
         * addToDOM values: 0 = do not add (false) 1 = append (true) 2 = replace
         * (this is done when the user choose to repair the value)
         */
        int count;
        MetaNode nodeMeta;
        
        if(node == null){
            throw new DataException("Cannot add null child.");
        } else if(this.children.contains(node)){
            throw new DataException("Element already contains this child.");
        } else if(!this.isNodeChildCandidate(node)){
            throw new DataException("Node cannot be added to this element.");
        }
        
        nodeMeta = node.getMetadata();
        count = 0;
        
        for(DataNode child: this.children){
            if(child instanceof DataValue){
                if(node instanceof DataValue){
                    throw new DataException("Element " + 
                            ((MetaElement)this.metadata).getName() + 
                            " already contains this child: " + 
                            ((DataValue)node).getValue() + " with value: " + 
                            ((DataValue)child).getValue());
                }
            } else if(child instanceof DataElement){
                if(node instanceof DataElement){
                    if(child.getMetadata().equals(nodeMeta)){
                        count++;
                    }
                }
            } else if(child instanceof DataAttribute){
                if(node instanceof DataAttribute){
                    if(child.getMetadata().equals(nodeMeta)){
                        throw new DataException("Element already contains attribute: " 
                                + ((MetaAttribute)nodeMeta).getName());
                    }
                }
            }
        } 
        
        if(nodeMeta instanceof MetaElement){
            if (nodeMeta.getVersion().equals(ConfigModeIntializer.COMMERCIAL) 
                    && (ConfigModeIntializer.CONFIGURATOR_MODE == ConfigModeIntializer.COMMUNITY_MODE || ConfigModeIntializer.CONFIGURATOR_MODE == ConfigModeIntializer.COMMUNITY_MODE_FILE_OPEN)) {
                addToDOM = 0;
            }
            if(count == ((MetaElement)nodeMeta).getMaxOccurrences()){
                throw new DataException("Maximum number of occurrences for " + 
                        ((MetaElement)nodeMeta).getName()+ " reached.");
            } else {
                if (addToDOM == 1) {
                    ((Element)this.node).appendChild(node.getNode());
                    Text textNode = this.owner.getDocument().createTextNode("\n");
                    this.node.appendChild(textNode);
                } else if (addToDOM == 2) {
                    replaceChild(node.getNode(), value);
                    Text textNode = this.owner.getDocument().createTextNode("\n");
                    this.node.appendChild(textNode);
                }
            }
        } else if(nodeMeta instanceof MetaAttribute){
            if (nodeMeta.getVersion().equals(ConfigModeIntializer.COMMERCIAL)
                    && (ConfigModeIntializer.CONFIGURATOR_MODE == ConfigModeIntializer.COMMUNITY_MODE || ConfigModeIntializer.CONFIGURATOR_MODE == ConfigModeIntializer.COMMUNITY_MODE_FILE_OPEN)) {
                addToDOM = 0;
            }
            if (addToDOM == 1) {
                ((Element)this.node).setAttributeNode((Attr)node.getNode());
            }
        } else if(nodeMeta instanceof MetaValue){
            if (addToDOM == 1) {
                ((Element)this.node).appendChild(node.getNode());
                assert this.owner != null;
                assert this.owner.getDocument() != null;
            } else if (addToDOM == 2) {
                replaceChild(node.getNode(), value);
                Text textNode = this.owner.getDocument().createTextNode("\n");
                this.node.appendChild(textNode);
            }
        }
        this.children.add(node);
        node.setParent(this);
        node.setOwner(this.owner);
        
        return node;
    }
    
    public void replaceChild(Node node, String value) {
        NodeList nodeList = ((Element) this.node).getChildNodes();
        boolean finished = false;
        for (int i = 0, len = nodeList.getLength(); i < len && !finished; i++) {
            Node n = nodeList.item(i);
            if (n.getNodeValue().equals(value)) {
                ((Element) this.node).replaceChild(node, n);
                finished = true;
            }
        }
    }

    @Override
    public void setOwner(DataConfiguration owner) {
        super.setOwner(owner);
        
        for(DataNode child: this.children){
            child.setOwner(owner);
        }
    }
    
    public DataNode addChild(DataNode node) throws DataException{
        return addChild(node, 1, null);
    }
    
    public void removeChild(DataNode node) throws DataException{
        int count;
        MetaNode nodeMeta;
        
        if(node == null){
            throw new DataException("Cannot remove null child.");
        } else if(!(this.children.contains(node))){
            throw new DataException("Element does not contain this child.");
        }
        nodeMeta = node.getMetadata();
        count = 0;
        
        for(DataNode child: this.children){
            if(child.getMetadata().equals(nodeMeta)){
                count++;
            }
            /*
            if(child instanceof DataValue){
                if(node instanceof DataValue){
                    count++;
                }
            } else if(child instanceof DataElement){
                if(node instanceof DataElement){
                    if(((MetaElement)child.getMetadata()).getName().equals(
                       ((MetaElement)nodeMeta).getName())) {
                        count++;
                    }
                }
            } else if(child instanceof DataAttribute){
                if(node instanceof DataAttribute){
                    if(((MetaAttribute)child.getMetadata()).getName().equals(
                       ((MetaAttribute)nodeMeta).getName())) {
                        count++;
                    }
                }
            }
            */
        }
        if(nodeMeta instanceof MetaElement){
            if(count == ((MetaElement)nodeMeta).getMinOccurrences()){
                throw new DataException("Minimum number of occurrences for " + 
                        ((MetaElement)nodeMeta).getName()+ " reached.");
            } else {
                if (ConfigModeIntializer.CONFIGURATOR_MODE == ConfigModeIntializer.COMMERCIAL_MODE) {
                    ((Element)this.node).removeChild(node.getNode());
                }
                else {
                    if (!nodeMeta.getVersion().equals(ConfigModeIntializer.COMMERCIAL)) {
                        if (!node.getOwner().getCommercialServices().contains(node.getNode())) {
                            ((Element) this.node).removeChild(node.getNode());
                        }
                    }
                }
            }
        } else if(nodeMeta instanceof MetaAttribute){
            if(((MetaAttribute)nodeMeta).isRequired()){
                throw new DataException("Cannot remove required attribute " + 
                        ((MetaAttribute)nodeMeta).getName()+ ".");
            } else {
                if (ConfigModeIntializer.CONFIGURATOR_MODE == ConfigModeIntializer.COMMERCIAL_MODE) {
                    ((Element)this.node).removeAttributeNode((Attr)node.getNode());
                }
                else {
                    if (!nodeMeta.getVersion().equals(ConfigModeIntializer.COMMERCIAL)) {
                        ((Element)this.node).removeAttributeNode((Attr)node.getNode());
                    }
                }
            }
        } else {
            ((Element)this.node).removeChild(node.getNode());
        }
        this.children.remove(node);
        //node.setParent(null);
        //node.setOwner(null);
        return;
    }
    
    public DataNode[] getChildren(){
        return this.children.toArray(new DataNode[this.children.size()]);
    }
    
    private boolean isNodeChildCandidate(DataNode node){
        MetaNode nodeMeta    = node.getMetadata();
        MetaNode[] metaNodes = ((MetaElement)this.metadata).getChildren();
        
        for(MetaNode mn: metaNodes){
            if(mn.equals(nodeMeta)){
                return true;
            }
        }
        return false;
    }
}
