% Create a network of 256 nodes with several levels of hierarchy
% Generated as presented in Arenas, Diaz-Guilera and Perez-Vicente (2006)
%
% Output
%   - adj   : adjacency matrix
%   - coords: gplot coordinates
%
% Author: Erwan Le Martelot
% Date: 31/03/11

function [adj,coords] = generate_h134()

    [adj1, coords1] = create_64_4;
    [adj2, coords2] = create_64_4;
    [adj3, coords3] = create_64_4;
    [adj4, coords4] = create_64_4;
    z = zeros(64,64);
    adj = [
        adj1, z ,z, z
        z, adj2, z, z
        z, z, adj3, z
        z, z, z, adj4
        ];
    
    for i=1:64
        coords2(i,1) = coords2(i,1) + 14;
        coords3(i,2) = coords3(i,2) + 14;
        coords4(i,1) = coords4(i,1) + 14;
        coords4(i,2) = coords4(i,2) + 14;
    end
    coords = [coords1; coords2; coords3; coords4];
    
    for i=1:256
        k = randi(256);
        while (k==i) || (adj(i,k)==1) 
            k = randi(256);;
        end
        adj(i,k) = 1;
        adj(k,i) = 1;
    end
    
end

function [adj, coords] = create_64_4()
    
    [adj1, coords1] = create_16_13;
    [adj2, coords2] = create_16_13;
    [adj3, coords3] = create_16_13;
    [adj4, coords4] = create_16_13;
    z = zeros(16,16);
    adj = [
        adj1, z ,z, z
        z, adj2, z, z
        z, z, adj3, z
        z, z, z, adj4
        ];
    
    for i=1:16
        coords2(i,1) = coords2(i,1) + 6;
        coords3(i,2) = coords3(i,2) + 6;
        coords4(i,1) = coords4(i,1) + 6;
        coords4(i,2) = coords4(i,2) + 6;
    end
    coords = [coords1; coords2; coords3; coords4];
    
    for i=1:64
        if i<=16
            targets = 17:64;
        elseif i<=32
            targets = [1:16, 33:64];
        elseif i<=48
            targets = [1:32, 49:64];
        else
            targets = 1:49;
        end
        k=1;
        while k <= length(targets)
            if adj(i, targets(k)) == 1
                targets(k) = [];
            else
                k = k + 1;
            end
        end
        while length(targets) > 44
            k = randi(length(targets));
            adj(i, targets(k)) = 1;
            adj(targets(k), i) = 1;
            targets(k) = [];
        end
    end
    
end

function [adj, coords] = create_16_13()
    
    adj = zeros(16,16);

    coords = [];
    for i=1:4
        for j=1:4
            coords = [coords; i j];
        end
    end
    
    for i=1:16
        if i==1
            targets = 2:16;
        elseif i==16
            targets = 1:15;
        else
            targets = [1:(i-1), (i+1):15];
        end
        k=1;
        while k <= length(targets)
            if adj(i, targets(k)) == 1
                targets(k) = [];
            else
                k = k + 1;
            end
        end
        while length(targets) > 2
            k = randi(length(targets));
            adj(i, targets(k)) = 1;
            adj(targets(k), i) = 1;
            targets(k) = [];
        end
    end
end
