package org.mg.gpsdownloader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

//============================================================================
// Name        : Tls12SocketFactory
// Author      : Matthias Gruenewald
// Copyright   : Copyright 2010-2021 Matthias Gruenewald
//
// This file is part of GeoDiscoverer.
//
// GeoDiscoverer is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// GeoDiscoverer is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with GeoDiscoverer.  If not, see <http://www.gnu.org/licenses/>.
//
//============================================================================
public class Tls12SocketFactory extends SSLSocketFactory {
   private static final String[] TLS_V12_ONLY = {"TLSv1.2"};

   final SSLSocketFactory delegate;

   public Tls12SocketFactory(SSLSocketFactory base) {
      this.delegate = base;
   }

   @Override
   public String[] getDefaultCipherSuites() {
      return delegate.getDefaultCipherSuites();
   }

   @Override
   public String[] getSupportedCipherSuites() {
      return delegate.getSupportedCipherSuites();
   }

   @Override
   public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
      return patch(delegate.createSocket(s, host, port, autoClose));
   }

   @Override
   public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
      return patch(delegate.createSocket(host, port));
   }

   @Override
   public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
      return patch(delegate.createSocket(host, port, localHost, localPort));
   }

   @Override
   public Socket createSocket(InetAddress host, int port) throws IOException {
      return patch(delegate.createSocket(host, port));
   }

   @Override
   public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
      return patch(delegate.createSocket(address, port, localAddress, localPort));
   }

   private Socket patch(Socket s) {
      if (s instanceof SSLSocket) {
         ((SSLSocket) s).setEnabledProtocols(TLS_V12_ONLY);
      }
      return s;
   }
}