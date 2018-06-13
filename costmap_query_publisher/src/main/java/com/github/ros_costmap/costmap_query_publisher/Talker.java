/*
 * Copyright (C) 2014 koralewski.
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

package com.github.rosjava.ros_costmap.costmap_query_publisher;

import com.github.ros_costmap.costmap_query_publisher.Costmap;
import geometry_msgs.Point;
import geometry_msgs.Pose;
import geometry_msgs.Quaternion;
import geometry_msgs.Vector3;
import org.bson.Document;
import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageFactory;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import std_msgs.ColorRGBA;
import std_msgs.Duration;
import std_msgs.Header;
import visualization_msgs.MarkerArray;
import visualization_msgs.Marker;

import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Publisher} {@link NodeMain}.
 */
public class Talker extends AbstractNodeMain {

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava/talker");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {

    final Publisher<MarkerArray> publisher =
        connectedNode.newPublisher("visualization_marker_array", MarkerArray._TYPE);
    // This CancellableLoop will be canceled automatically when the node shuts
    // down.
    connectedNode.executeCancellableLoop(new CancellableLoop() {
      private int sequenceNumber;

      @Override
      protected void setup() {
        sequenceNumber = 0;
      }

      @Override
      protected void loop() throws InterruptedException {
        MarkerArray str = publisher.newMessage();
        Costmap costmap = new Costmap("172.17.0.2",
                27017, "roslog",
                "1526459265472506_cram_location_costmap");
        Time time = new Time(1526459266,49365);
        Document document = costmap.getCostmapMarkers(1526459353.39127);
        ArrayList<Document> markerList = (ArrayList<Document>)document.get("markers");
        str.setMarkers(createMarkerArray(document));
        publisher.publish(str);
        sequenceNumber++;
        Thread.sleep(1000);
      }
    });
  }

  private ArrayList<Marker> createMarkerArray(Document markerArrayDocument){
    NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
    MessageFactory messageFactory = nodeConfiguration.getTopicMessageFactory();

    ArrayList<Document> markerDocumentList = (ArrayList<Document>) markerArrayDocument.get("markers");
    ArrayList<Marker> markerArrayList = new ArrayList<>();

    for (Document markerDocument: markerDocumentList) {
      markerArrayList.add(createMarkerMessage(markerDocument,messageFactory));
    }

    return markerArrayList;
  }

  private Marker createMarkerMessage(Document marker, MessageFactory messageFactory){


    Marker msg = messageFactory.newFromType(Marker._TYPE);
    Vector3 vector3Msg = messageFactory.newFromType(Vector3._TYPE);
    Pose poseMsg = messageFactory.newFromType(Pose._TYPE);
    Quaternion quaternionMsg  = messageFactory.newFromType(Quaternion._TYPE);
    Header header =  messageFactory.newFromType(Header._TYPE);
    msg.setMeshUseEmbeddedMaterials((Boolean) marker.get("mesh_use_embedded_materials"));

    Document markerScale =  (Document)marker.get("scale");
    vector3Msg.setX(markerScale.getDouble("x"));
    vector3Msg.setY(markerScale.getDouble("y"));
    vector3Msg.setZ(markerScale.getDouble("z"));
    msg.setScale(vector3Msg);

    msg.setFrameLocked((Boolean) marker.get("frame_locked"));
    msg.setColor(transformIntoColorRGBAMsg((Document) marker.get("color"), messageFactory));

    msg.setText(marker.getString("text"));

    Document markerPose = (Document) marker.get("pose");
    Document markerPoseOrientation = (Document) markerPose.get("orientation");

    quaternionMsg.setW(markerPoseOrientation.getDouble("w"));
    quaternionMsg.setX(markerPoseOrientation.getDouble("x"));
    quaternionMsg.setY(markerPoseOrientation.getDouble("y"));
    quaternionMsg.setZ(markerPoseOrientation.getDouble("z"));
    poseMsg.setOrientation(quaternionMsg);


    poseMsg.setPosition(transformIntoPointMsg((Document) markerPose.get("position"),messageFactory));
    msg.setPose(poseMsg);

    msg.setMeshResource(marker.getString("mesh_resource"));

    Document headerDocument = (Document)marker.get("header");
    Date date = (Date) headerDocument.get("stamp");
    header.setStamp(new Time(date.getTime()/1000.0));
    header.setFrameId(headerDocument.getString("frame_id"));
    header.setSeq(headerDocument.getInteger("seq"));
    msg.setHeader(header);

    ArrayList<Document> colorsDocumentList = (ArrayList<Document>) marker.get("colors");
    ArrayList<ColorRGBA> colorsMsg = new ArrayList<>();

    for (Document colorsDocument: colorsDocumentList) {
      colorsMsg.add(transformIntoColorRGBAMsg(colorsDocument,messageFactory));
    }

    msg.setColors(colorsMsg);

    ArrayList<Document> pointsDocumentList = (ArrayList<Document>) marker.get("points");
    ArrayList<Point> pointsMsg = new ArrayList<>();

    for (Document pointsDocument: pointsDocumentList) {
      pointsMsg.add(transformIntoPointMsg(pointsDocument,messageFactory));
    }

    msg.setPoints(pointsMsg);

    msg.setAction(marker.getInteger("action"));
    msg.setLifetime(new org.ros.message.Duration(marker.getDouble("lifetime")));
    msg.setNs(marker.getString("ns"));
    msg.setType(marker.getInteger("type"));
    msg.setId(marker.getInteger("id"));

    return msg;
  }

  private ColorRGBA transformIntoColorRGBAMsg(Document colorDocument, MessageFactory messageFactory){
    ColorRGBA colorRGBAMsg = messageFactory.newFromType(ColorRGBA._TYPE);
    colorRGBAMsg.setA(((Double) colorDocument.get("a")).floatValue());
    colorRGBAMsg.setR(((Double) colorDocument.get("r")).floatValue());
    colorRGBAMsg.setG(((Double) colorDocument.get("g")).floatValue());
    colorRGBAMsg.setB((((Double) colorDocument.get("b")).floatValue()));
    return colorRGBAMsg;
  }

  private Point transformIntoPointMsg(Document pointDocument, MessageFactory messageFactory){
    Point pointMsg = messageFactory.newFromType(Point._TYPE);
    pointMsg.setX(pointDocument.getDouble("x"));
    pointMsg.setY(pointDocument.getDouble("y"));
    pointMsg.setZ(pointDocument.getDouble("z"));

    return pointMsg;

  }

}


