package org.wso2.carbon.identity.organization.mgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.organization.mgt.endpoint.dto.AttributeDTO;
import org.wso2.carbon.identity.organization.mgt.endpoint.dto.UserstoreConfigDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class OrganizationAddDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String description = null;
  
  
  private String parentId = null;
  
  
  private List<AttributeDTO> attributes = new ArrayList<AttributeDTO>();
  
  
  private List<UserstoreConfigDTO> userstoreConfigs = new ArrayList<UserstoreConfigDTO>();

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("parentId")
  public String getParentId() {
    return parentId;
  }
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("attributes")
  public List<AttributeDTO> getAttributes() {
    return attributes;
  }
  public void setAttributes(List<AttributeDTO> attributes) {
    this.attributes = attributes;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("userstoreConfigs")
  public List<UserstoreConfigDTO> getUserstoreConfigs() {
    return userstoreConfigs;
  }
  public void setUserstoreConfigs(List<UserstoreConfigDTO> userstoreConfigs) {
    this.userstoreConfigs = userstoreConfigs;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class OrganizationAddDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  parentId: ").append(parentId).append("\n");
    sb.append("  attributes: ").append(attributes).append("\n");
    sb.append("  userstoreConfigs: ").append(userstoreConfigs).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
