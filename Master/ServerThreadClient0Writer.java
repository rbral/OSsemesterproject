package yg.Master;

import yg.Job;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Master's WRITER threads to clients
 * takes the finished jobs from the finished jobs array
 * and writes it back to the correct client that sent it
 * over the socket
 */

public class ServerThreadClient0Writer implements Runnable {
    private ServerSocket serverSocket = null;
    private ServerSharedMemory sharedMemory;
    int clientId;
    Object doneJobs_LOCK;
    ArrayList<Job> doneJobs;

    public ServerThreadClient0Writer(ServerSocket serverSocket, int clientId, ServerSharedMemory sharedMemory) {
        this.serverSocket = serverSocket;
        this.sharedMemory = sharedMemory;
        this.clientId = clientId;
        this.doneJobs_LOCK = sharedMemory.getDoneJobs_LOCK();
        this.doneJobs = sharedMemory.getDoneJobs();
    }

    @Override
    public void run() {
        try (Socket clientSocket = serverSocket.accept();
             // object streams:
             ObjectOutputStream objectOut = new ObjectOutputStream(clientSocket.getOutputStream());

        ) {
            while (true)
            {
                ArrayList<Job> currDoneJobsForClient0;

                synchronized (sharedMemory.getClient0DoneJobs_LOCK())
                {
                    currDoneJobsForClient0 = new ArrayList<>(sharedMemory.getClient0DoneJobs());
                }

                for (Job currDoneJob : currDoneJobsForClient0)
                {
                    // remove each one from original array
                    synchronized (sharedMemory.getClient0DoneJobs_LOCK())
                    {
                        sharedMemory.getClient0DoneJobs().remove(currDoneJob);
                    }

                    // write it to the client0 socket:
                    System.out.println("ServerTClient0Writer: Sending to client0 socket: " +
                            "Client: " + currDoneJob.getClient() + ", Type: " + currDoneJob.getType() +
                            "ID: " + currDoneJob.getID());
                    objectOut.writeObject(currDoneJob);
                    objectOut.flush();
                }

            }
            /*while((sharedMemory.getDoneJobs() != null))
            {
                ArrayList<Job> currDoneJobs = new ArrayList<>(sharedMemory.getDoneJobs());

                for (Job currDoneJob : currDoneJobs)
                    System.out.println("Sending finished job " + currDoneJob.getID() + " type " + currDoneJob.getType() + " to client " + currDoneJob.getClient());
                {
                    synchronized(doneJobs_LOCK)
                    {
                        objectOut.writeObject(sharedMemory.getDoneJobs().remove(0));
                        objectOut.flush();
                    }
                }
            }*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}